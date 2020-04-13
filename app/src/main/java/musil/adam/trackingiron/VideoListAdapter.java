package musil.adam.trackingiron;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {

    class VideoViewHolder extends RecyclerView.ViewHolder{
        private final TextView videoItemView;


        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoItemView = itemView.findViewById(R.id.textView);
        }
    }

    private final LayoutInflater inflater;
    private List<Video> vids;

    VideoListAdapter(Context context){
        inflater = LayoutInflater.from(context);
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = inflater.inflate(R.layout.recycler_item, parent, false);
        return new VideoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position){
        if(vids != null){
            Video current = vids.get(position);
            holder.videoItemView.setText(current.getName());
        }else{
            holder.videoItemView.setText("No Vid No did");
        }
    }

    @Override
    public int getItemCount(){
        if(vids != null)
            return vids.size();
        else
            return 0;
    }

    void setVids(List<Video> vids){
        this.vids = vids;
        notifyDataSetChanged();
    }


}
