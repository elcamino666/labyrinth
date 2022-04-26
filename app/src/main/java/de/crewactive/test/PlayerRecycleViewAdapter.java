package de.crewactive.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Adapter for RecycleView
 */
public class PlayerRecycleViewAdapter extends RecyclerView.Adapter<PlayerRecycleViewAdapter.ViewHolder> {

    private ArrayList<PlayerModel> players = new ArrayList<>();

    private Context context;

    private DatabaseHelper databaseHelper;

    /**
     * Constructor of RecycleViewAdapter
     *
     * @param context context
     */
    public PlayerRecycleViewAdapter(Context context) {
        this.context = context;
    }

    /**
     * Initialize holder and infiltrate it
     *
     * @param parent   parent
     * @param viewType view type
     * @return holder object
     */
    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        databaseHelper = new DatabaseHelper(context);
        return holder;
    }

    /**
     * Binding the view holder
     *
     * @param holder   holder object
     * @param position position of item in holder
     */
    @Override
    public void onBindViewHolder(@NonNull @NotNull PlayerRecycleViewAdapter.ViewHolder holder, int position) {
        holder.nameItem.setText(players.get(position).getName());
        holder.timeItem.setText(players.get(position).getTime());
        holder.placeItem.setText(String.valueOf(position + 1));
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.deleteOne(players.get(position));
                setPlayers((ArrayList<PlayerModel>) databaseHelper.getEveryOne());
                notifyDataSetChanged();
            }
        });
    }

    /**
     * getter of size of items in holder
     *
     * @return players number that exist in list
     */
    @Override
    public int getItemCount() {
        return players.size();
    }

    /**
     * setter for players and notify for changed data
     *
     * @param players players array list
     */
    public void setPlayers(ArrayList<PlayerModel> players) {
        this.players = players;
        notifyDataSetChanged();
    }

    /**
     * View Holder Class for Recycle View
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameItem;
        private TextView placeItem;
        private TextView timeItem;

        private RelativeLayout parent;

        /**
         * Constructor von View Holder
         *
         * @param itemView item View of Holder
         */
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            nameItem = itemView.findViewById(R.id.nameItem);
            placeItem = itemView.findViewById(R.id.placeItem);
            timeItem = itemView.findViewById(R.id.timeItem);
            parent = itemView.findViewById(R.id.parentItem);
        }
    }
}
