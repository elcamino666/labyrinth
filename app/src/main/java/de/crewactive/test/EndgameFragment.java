package de.crewactive.test;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Fragment that shows top records in a list
 */
public class EndgameFragment extends Fragment implements ISaveNameDialogListener {

    private Singleton singleton;                            // Global variables saved in singleton pattern
    private NavController navController;                    // navigation Controller
    private SaveNameDialog dialog = new SaveNameDialog();   // Dialog to save name from player
    private TextView noItem;                                // Text View
    private RecyclerView playerRecView;                     // Recycle View --> list of top Records
    private DatabaseHelper databaseHelper;                  // DatabaseHelper --> remove add Players to list

    /**
     * Required empty public constructor for EndgameFragment
     */
    public EndgameFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Infiltrate the layout for this fragment
     *
     * @param inflater           inflater
     * @param container          container
     * @param savedInstanceState saved Instance State
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_endgame, container, false);
    }

    /**
     * Initialize variables, navigation controller and sets OnClickListeners
     *
     * @param view               view
     * @param savedInstanceState saved Instance State
     */
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        playerRecView = view.findViewById(R.id.playerRecycleView);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recycle_view_divider));
        playerRecView.addItemDecoration(dividerItemDecoration);

        singleton = Singleton.getInstance();

        databaseHelper = new DatabaseHelper(getActivity());

        ShowPlayerOnListView(databaseHelper);
        dialog.setCallBack(this);
        if (singleton.getTimePlayed() != null)
            dialog.show(requireActivity().getSupportFragmentManager(), "get name");
        noItem = view.findViewById(R.id.noItemTxt);
        if (databaseHelper.getEveryOne().size() == 0) noItem.setVisibility(View.VISIBLE);
        else noItem.setVisibility(View.GONE);

    }

    /**
     * Shows all players records on Recycle List
     *
     * @param databaseHelper obj to handle database operations
     */
    private void ShowPlayerOnListView(DatabaseHelper databaseHelper) {
        PlayerRecycleViewAdapter adapter = new PlayerRecycleViewAdapter(getActivity());
        adapter.setPlayers((ArrayList<PlayerModel>) databaseHelper.getEveryOne());
        playerRecView.setAdapter(adapter);
        playerRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    /**
     * Apply name of Player and adds to db
     *
     * @param name name of player
     */
    @Override
    public void applyName(String name) {
        String timePlayed = singleton.getTimePlayed();

        PlayerModel playerModel;

        try {
            playerModel = new PlayerModel(-1, name, timePlayed);
        } catch (Exception e) {
            playerModel = new PlayerModel(-1, "error", "Error time played");
        }
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.addOne(playerModel);
        noItem.setVisibility(View.GONE);
        ShowPlayerOnListView(databaseHelper);
    }

    /**
     * Add Custom App bar Menu
     *
     * @param menu     appBar menu
     * @param inflater inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.end_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Click listener on App bar menu items
     *
     * @param item App bar menu item
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {

        if (item.getItemId() == R.id.restartAppBar) {
            NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.startFragment, true).build();
            navController.navigate(R.id.action_endgameFragment_to_startFragment, null, navOptions);
            return true;
        }
        return false;
    }
}