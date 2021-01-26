package yelm.io.yelm.support_stuff;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class ItemOffsetDecorationBottom extends RecyclerView.ItemDecoration {

    private int offset;

    public ItemOffsetDecorationBottom(int offset) {
        this.offset = offset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        // Добавление отступов к нулевому элементу
        if (parent.getChildAdapterPosition(view) == (state.getItemCount()-1)) {
            //outRect.right = offset;
            //outRect.left = offset;
            //outRect.top = offset;
            outRect.bottom = offset;
        }
    }
}


