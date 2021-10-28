package co.edu.javeriana.motivarche.ui.scanner;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import co.edu.javeriana.motivarche.ImagesActivity;
import co.edu.javeriana.motivarche.R;
import co.edu.javeriana.motivarche.Utils;
import co.edu.javeriana.motivarche.common.ARCore.BackgroundRenderer;
import co.edu.javeriana.motivarche.common.ARCore.CameraPermissionHelper;
import co.edu.javeriana.motivarche.common.ARCore.DisplayRotationHelper;
import co.edu.javeriana.motivarche.common.ARCore.FullScreenHelper;
import co.edu.javeriana.motivarche.common.ARCore.SnackbarHelper;
import co.edu.javeriana.motivarche.common.ARCore.TrackingStateHelper;

public class AugmentedImageActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private static final String TAG = AugmentedImageActivity.class.getSimpleName();

    List<UploadImage> imagenes = new ArrayList<UploadImage>();


    Bitmap bitmapImg;
    private GLSurfaceView surfaceView;
    private ImageView fitToScanView;
    private RequestManager glideRequestManager;
    private DatabaseReference mDatabaseRef;
    private boolean installRequested;
    private Session session;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private DisplayRotationHelper displayRotationHelper;
    private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);
    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final AugmentedImageRenderer augmentedImageRenderer = new AugmentedImageRenderer();
    private boolean shouldConfigureSession = false;
    // Augmented image and its associated center pose anchor, keyed by index of the augmented image in
    // the database.
    private final Map<Integer, Pair<AugmentedImage, Anchor>> augmentedImageMap = new HashMap<>();
    private PhysicsController physicsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("images");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapshot : snapshot.getChildren()) {
                    UploadImage uploadImage = postSnapshot.getValue(UploadImage.class);
                    imagenes.add(uploadImage);
                    Log.i("firebase", uploadImage.getNameImage() + " " + uploadImage.getUrlImage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AugmentedImageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        setContentView(R.layout.activity_scanner);
        surfaceView = findViewById(R.id.surfaceview);

        displayRotationHelper = new DisplayRotationHelper(this);

        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setWillNotDraw(false);

        fitToScanView = findViewById(R.id.image_view_fit_to_scan);
        glideRequestManager = Glide.with(this);
        glideRequestManager
                .load(Uri.parse("file:///android_asset/fit_to_scan.png"))
                .into(fitToScanView);

        installRequested = false;

    }

    @Override
    protected void onDestroy() {
        if (session != null) {
            session.close();
            session = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                session = new Session( this);

            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Por favor instale ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Actualice ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Actualice la version de la aplicacion";
                exception = e;
            } catch (Exception e) {
                message = "Este dispositivo no soporta ARCore";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(this, message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }

            shouldConfigureSession = true;
        }

        if (shouldConfigureSession) {
            configureSession();
            shouldConfigureSession = false;
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper.showError(this, "Camara no disponible. Tratando de reiniciar la aplicacion.");
            session = null;
            return;
        }
        surfaceView.onResume();
        displayRotationHelper.onResume();

        fitToScanView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(
                    this, "Se requiere permisos de la camara para ejecutar esta funcionalidad", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread(this);
            augmentedImageRenderer.createOnGlThread(this);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());
            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            session.resume();
            Frame frame = session.update();
            Camera camera = frame.getCamera();

            // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
            trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

            // If frame is ready, render camera preview image to the GL surface.
            backgroundRenderer.draw(frame);

            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            final float[] colorCorrectionRgba = new float[4];
            frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

            // Visualize augmented images.
            drawAugmentedImages(frame, projmtx, viewmtx, colorCorrectionRgba);
        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    private void configureSession() {
        Config config = new Config(session);
        config.setFocusMode(Config.FocusMode.AUTO);
        if (!setupAugmentedImageDatabase(config)) {
            messageSnackbarHelper.showError(this, "Could not setup augmented image database");
        }
        session.configure(config);
    }

    private void drawAugmentedImages(
            Frame frame, float[] projmtx, float[] viewmtx, float[] colorCorrectionRgba) {
        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);

        // Iterate to update augmentedImageMap, remove elements we cannot draw.

        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    String text = String.format("Imagen detectada %s", augmentedImage.getName());
                    messageSnackbarHelper.showMessage(this, text);
                    break;

                case TRACKING:
                    // Switch to UI Thread to update View
                    this.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    fitToScanView.setVisibility(View.GONE);
                                }
                            });

                    // Create a new anchor for newly found images
                    if (!augmentedImageMap.containsKey(augmentedImage.getIndex())) {
                        Anchor centerPoseAnchor = augmentedImage.createAnchor(augmentedImage.getCenterPose());
                        augmentedImageMap.put(
                                augmentedImage.getIndex(), Pair.create(augmentedImage, centerPoseAnchor));

                        physicsController = new PhysicsController(this);
                    } else {
                        Pose ballPose = physicsController.getBallPose();
                        augmentedImageRenderer.updateAndyPose(ballPose);


                        // Use real world gravity, (0, -10, 0), as gravity
                        // Convert to Physics world coordinate(maze mesh has to be static)
                        // Use it as a force to move the ball
                        Pose worldGravityPose = Pose.makeTranslation(0, -10f, 0);
                        Pose mazeGravityPose = augmentedImage.getCenterPose().inverse().compose(worldGravityPose);
                        float mazeGravity[] = mazeGravityPose.getTranslation();
                        physicsController.applyGravityToBall(mazeGravity);
                        physicsController.updatePhysics();
                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage.getIndex());
                    break;

                default:
                    break;
            }
        }


        // Draw all images in augmentedImageMap
        for (Pair<AugmentedImage, Anchor> pair : augmentedImageMap.values()) {
            AugmentedImage augmentedImage = pair.first;
            Anchor centerAnchor = augmentedImageMap.get(augmentedImage.getIndex()).second;
            switch (augmentedImage.getTrackingState()) {
                case TRACKING:
                    augmentedImageRenderer.draw(
                            viewmtx, projmtx, augmentedImage, centerAnchor, colorCorrectionRgba);
                    break;
                default:
                    break;
            }
        }

    }


    private boolean setupAugmentedImageDatabase(Config config) {
        AugmentedImageDatabase augmentedImageDatabase = new AugmentedImageDatabase(session);

        if(!Utils.arImages.isEmpty()){
            for(Map.Entry<String,Bitmap> entrySet: Utils.arImages.entrySet()){
                augmentedImageDatabase.addImage(entrySet.getKey(), entrySet.getValue());
            }

            config.setAugmentedImageDatabase(augmentedImageDatabase);
            return true;
        }else{
            return false;
        }
    }

}
