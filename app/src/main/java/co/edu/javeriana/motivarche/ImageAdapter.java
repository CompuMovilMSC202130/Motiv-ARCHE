package co.edu.javeriana.motivarche;


import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.squareup.picasso.Picasso;

import java.util.List;

import co.edu.javeriana.motivarche.ui.scanner.UploadImage;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<UploadImage> mUploads;


    public ImageAdapter(Context context,List<UploadImage>uploads){
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item,parent,false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        UploadImage currentImage = mUploads.get(position);
        holder.textViewName.setText(currentImage.getNameImage());

        Log.i("URL",currentImage.getUrlImage());

        Picasso.with(mContext)
                .load(currentImage.getUrlImage())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerInside()
                .into(holder.imageView);
    }


    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewName;
        public ImageView imageView;

        public ImageViewHolder(View itemView){
            super(itemView);

            textViewName = itemView.findViewById(R.id.text_view_name);
            imageView = itemView.findViewById(R.id.image_view_upload);
        }
    }

}
