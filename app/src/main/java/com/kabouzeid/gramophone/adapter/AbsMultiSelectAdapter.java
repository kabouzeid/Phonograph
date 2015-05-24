package com.kabouzeid.gramophone.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialcab.MaterialCab;
import com.kabouzeid.gramophone.interfaces.CabHolder;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMultiSelectAdapter<VH extends RecyclerView.ViewHolder, I> extends RecyclerView.Adapter<VH> implements MaterialCab.Callback {
    private final CabHolder cabHolder;
    private MaterialCab cab;
    private ArrayList<I> checked;
    private int menuRes;

    public AbsMultiSelectAdapter(CabHolder cabHolder, int menuRes) {
        this.cabHolder = cabHolder;
        checked = new ArrayList<>();
        this.menuRes = menuRes;
    }

    protected void toggleChecked(final int position) {
        if (cabHolder != null) {
            openCabIfNecessary();
            I identifier = getIdentifier(position);
            if (!checked.remove(identifier)) checked.add(identifier);
            notifyItemChanged(position);
            if (checked.isEmpty()) cab.finish();
        }
    }

    private void openCabIfNecessary() {
        if (cab == null || !cab.isActive()) {
            cab = cabHolder.openCab(menuRes, this);
        }
    }

    private void uncheckAll() {
        checked.clear();
        notifyDataSetChanged();
    }

    protected boolean isChecked(I identifier) {
        return checked.contains(identifier);
    }

    protected boolean isInQuickSelectMode() {
        return cab != null && cab.isActive();
    }

    @Override
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem menuItem) {
        onMultipleItemAction(menuItem, new ArrayList<>(checked));
        cab.finish();
        uncheckAll();
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab materialCab) {
        uncheckAll();
        return true;
    }

    protected abstract I getIdentifier(int position);

    protected abstract void onMultipleItemAction(MenuItem menuItem, ArrayList<I> selection);
}
