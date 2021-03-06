package musil.adam.trackingiron;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * adapter videa pro zobrazeni v recyclerview
 */

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {

    class VideoViewHolder extends RecyclerView.ViewHolder{
        private final TextView videoItemView;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoItemView = itemView.findViewById(R.id.textView);
            //normalni stisknuti
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(v, getAdapterPosition());
                }
            });
            //dlouhe stisknuti
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longClickListener.onItemClick(v, getAdapterPosition());
                    return true;
                }
            });
        }
    }

    private final LayoutInflater inflater;
    private List<Video> vids;
    private static ClickListener clickListener;
    private static LongClickListener longClickListener;

    VideoListAdapter(Context context){
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = inflater.inflate(R.layout.recycler_item, parent, false);
        return new VideoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position){
        if(vids != null){
            Video current = vids.get(position);
            holder.videoItemView.setText(current.getDisplayName());
        }else{
            holder.videoItemView.setText("");
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

    Video getVideoAtPosition(int position){
        return vids.get(position);
    }

    void setOnItemClickListener(ClickListener clickListener) {
        VideoListAdapter.clickListener = clickListener;
    }

    void setLongClickListener(LongClickListener clickListener){
        VideoListAdapter.longClickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(View v, int position);
    }

    public interface LongClickListener{
        void onItemClick(View v, int position);
    }


}
