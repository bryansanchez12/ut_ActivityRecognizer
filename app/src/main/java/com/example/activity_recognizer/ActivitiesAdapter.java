package com.example.activity_recognizer;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {


    private ArrayList<ArrayList<String>> activitiesTime = new ArrayList<>();

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param activitiesList ArrayList[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public ActivitiesAdapter(ArrayList<ArrayList<String>> activitiesList){
        activitiesTime = activitiesList;
    }

    /**
     * Inner class that holds the view for each row and the content each row has
     */
    public static class ViewHolder extends RecyclerView.ViewHolder  {

        private final TextView activityInf;
        private final TextView activityTime;
        public ImageView img;

        /**
         * The row information is defined
         * @param itemView of the RecyclerView in this case
         */
        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            activityInf = itemView.findViewById(R.id.activityInf);
            activityTime = itemView.findViewById(R.id.activityTime);
            img = itemView.findViewById(R.id.img);
        }

    }


    /**
     * It creates a new view, wich defines de UI of the list item
     * @param parent the parent View group
     * @param viewType the integer value of the element
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View activityView = inflater.inflate(R.layout.activityrow,parent,false);
        return new ViewHolder(activityView);
    }

    /**
     * It gets the element from the dataset from the position and replace
     * the contents of the view with that element.
     * it sets the image as well according to the
     * @param holder The ViewHolder of the (Recyclerview)
     * @param position position of the information
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        String act = activitiesTime.get(position).get(0); // index 1 of array
        String time = activitiesTime.get(position).get(1); // index 2 of array

        TextView inf = holder.activityInf;
        TextView tme = holder.activityTime;
        ImageView photo = holder.img;

        inf.setText(act);
        tme.setText(time);
        switch (act){
            case "walking":
                photo.setImageResource(R.mipmap.wallking);
                break;
            case "standing":
                photo.setImageResource(R.mipmap.standing);
                break;
            case "jogging":
                photo.setImageResource(R.mipmap.jogging);
                break;
            case"sitting":
                photo.setImageResource(R.mipmap.sitting);
                break;
            case "biking":
                photo.setImageResource(R.mipmap.biking);
                break;
            case "upstairs":
                photo.setImageResource(R.mipmap.up);
                break;
            case "downstairs":
                photo.setImageResource(R.mipmap.down);

        }
    }

    /**
     * Get the item acount
     * @return the size of the Dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return activitiesTime.size();
    }


}
