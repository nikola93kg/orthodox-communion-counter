package org.orthodox.communioncounter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.orthodox.communioncounter.model.LiturgyRecord;

import java.util.List;

public class LiturgyAdapter extends BaseAdapter {

    private final Context context;
    private List<LiturgyRecord> recordList;
    private final LayoutInflater inflater;

    public LiturgyAdapter(Context context, List<LiturgyRecord> recordList) {
        this.context = context;
        this.recordList = recordList;
        this.inflater = LayoutInflater.from(context);
    }

    public void updateData(List<LiturgyRecord> newRecords) {
        this.recordList = newRecords;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return recordList != null ? recordList.size() : 0;
    }

    @Override
    public LiturgyRecord getItem(int position) {
        return recordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return recordList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_liturgy_record, parent, false);
            holder = new ViewHolder();
            holder.tvFeast = convertView.findViewById(R.id.tv_record_feast);
            holder.tvDate = convertView.findViewById(R.id.tv_record_date);
            holder.tvCount = convertView.findViewById(R.id.tv_record_count);
            holder.tvNotes = convertView.findViewById(R.id.tv_record_notes);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LiturgyRecord record = getItem(position);

        holder.tvFeast.setText(record.getFeastName());
        String fullDate = (record.getDayName() != null && !record.getDayName().isEmpty())
                ? record.getDayName() + ", " + record.getDateFormatted()
                : record.getDateFormatted();
        holder.tvDate.setText(fullDate);
        holder.tvCount.setText(String.valueOf(record.getCount()));

        String notes = record.getNotes();
        if (notes != null && !notes.trim().isEmpty()) {
            holder.tvNotes.setVisibility(View.VISIBLE);
            holder.tvNotes.setText("Белешка: " + notes);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvFeast;
        TextView tvDate;
        TextView tvCount;
        TextView tvNotes;
    }
}
