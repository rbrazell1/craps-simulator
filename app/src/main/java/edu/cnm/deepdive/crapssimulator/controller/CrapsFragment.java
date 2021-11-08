package edu.cnm.deepdive.crapssimulator.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.snackbar.Snackbar;
import edu.cnm.deepdive.crapssimulator.R;
import edu.cnm.deepdive.crapssimulator.databinding.FragmentCrapsBinding;
import edu.cnm.deepdive.crapssimulator.model.Snapshot;
import edu.cnm.deepdive.crapssimulator.viewmodel.CrapsViewModel;
import java.util.HashMap;
import java.util.Map;

public class CrapsFragment extends Fragment {

  private final Map<Integer, Runnable> actions = new HashMap<>();

  private FragmentCrapsBinding binding;
  private CrapsViewModel viewModel;
  private boolean running;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentCrapsBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(this).get(CrapsViewModel.class);
    getLifecycle().addObserver(viewModel);
    LifecycleOwner owner = getViewLifecycleOwner();
    viewModel.getSnapshot().observe(owner, this::updateSimulationDisplay);
    viewModel.getRunning().observe(owner, this::setRunning);
    viewModel.getFinishing().observe(owner, this::updateWaiting);
    viewModel.getThrowable().observe(owner, this::displayError);
    buildMenuActionsMap();
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_main, menu);
  }

  @Override
  public void onPrepareOptionsMenu(@NonNull Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.action_play_once).setVisible(!running);
    menu.findItem(R.id.action_play_fast).setVisible(!running);
    menu.findItem(R.id.action_pause).setVisible(running);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int itemId = item.getItemId();
    Runnable action = actions.get(itemId);
    boolean handled;
    if (action != null) {
      action.run();
      handled = true;
    } else {
      handled = super.onOptionsItemSelected(item);
    }
    return handled;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void updateSimulationDisplay(Snapshot snapshot) {
    // TODO Update rolls.
    long wins = snapshot.getWins();
    long rounds = snapshot.getRounds();
    double winningPercentage = (rounds > 0) ? (100.0 * wins / rounds) : 0;
    binding.summary.setText(getString(R.string.summary_format, wins, rounds, winningPercentage));
  }

  private void updateWaiting(Boolean finishing) {
    binding.waiting.setVisibility((finishing != null && finishing) ? View.VISIBLE : View.GONE);
  }

  private void displayError(Throwable throwable) {
    Snackbar
        .make(binding.getRoot(), getString(R.string.error_message_format, throwable.getMessage()),
            Snackbar.LENGTH_LONG)
        .show();
  }

  private void buildMenuActionsMap() {
    actions.clear();
    actions.put(R.id.action_play_once, viewModel::simulateBatch);
    actions.put(R.id.action_play_fast, viewModel::simulateFast);
    actions.put(R.id.action_pause, viewModel::stop);
    actions.put(R.id.action_reset, viewModel::reset);
    actions.put(R.id.action_settings, this::openSettings);
  }

  private void setRunning(boolean running) {
    this.running = running;
    //noinspection ConstantConditions
    getActivity().invalidateOptionsMenu();
  }

  private void openSettings() {
    Navigation
        .findNavController(binding.getRoot())
        .navigate(CrapsFragmentDirections.openSettings());
  }

}