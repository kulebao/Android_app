package com.cocobabys.adapter;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.BindedNumInfo;
import com.cocobabys.dlgmgr.DlgMgr;
import com.cocobabys.utils.Utils;

public class BindedNumInfoListAdapter extends BaseAdapter {
    private final Context context;
    private List<BindedNumInfo> infoList;
    private AlertDialog dialog;

    public void setLocationInfoList(List<BindedNumInfo> list) {
        this.infoList = list;
    }

    public BindedNumInfoListAdapter(Context activityContext, List<BindedNumInfo> list) {
        this.context = activityContext;
        infoList = list;
    }

    public void clear() {
        infoList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return infoList.size();
    }

    @Override
    public BindedNumInfo getItem(int position) {
        return infoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            FlagHolder flagholder = this.new FlagHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.bindednum_item, null);
            flagholder.numView = (TextView) convertView.findViewById(R.id.numView);
            flagholder.nicknameView = (TextView) convertView.findViewById(R.id.nicknameView);
            flagholder.changeView = (TextView) convertView.findViewById(R.id.changeView);
            flagholder.deleteView = (TextView) convertView.findViewById(R.id.deleteView);

            setDataToViews(position, flagholder);
            convertView.setTag(flagholder);
        } else {
            FlagHolder flagholder = (FlagHolder) convertView.getTag();
            if (flagholder != null) {
                setDataToViews(position, flagholder);
            }
        }

        return convertView;
    }

    private void setDataToViews(final int position, FlagHolder flagholder) {
        Resources resources = context.getResources();
        final BindedNumInfo info = infoList.get(position);
        flagholder.numView.setText(String.format(resources.getString(R.string.location_num),
                info.getPhone_num()));
        flagholder.nicknameView.setText(String.format(resources.getString(R.string.nichname),
                info.getNickname()));

        flagholder.changeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DDD pos", "position =" + position);
                showChangeDialog(position);
            }
        });

        flagholder.deleteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DDD pos", "position =" + position);
                Utils.showTwoBtnResDlg(R.string.delete_binded_num, context,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataMgr.getInstance().deleteBindedNumInfo(info.getId());
                                infoList.remove(position);
                                notifyDataSetChanged();
                            }
                        });
            }
        });
    }

    private void showChangeDialog(final int pos) {
        LayoutInflater factory = LayoutInflater.from(context);
        final View textEntryView = factory.inflate(R.layout.location_text_entry, null);
        final EditText nicknameEdit = (EditText) textEntryView.findViewById(R.id.nichname_edit);
        final EditText numEdit = (EditText) textEntryView.findViewById(R.id.location_num_edit);

        final BindedNumInfo info = getItem(pos);
        nicknameEdit.setText(info.getNickname());
        numEdit.setText(info.getPhone_num());

        dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.change_lbs_num)
                .setView(textEntryView)
                .setPositiveButton(R.string.change,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handleClickChangeBtn(nicknameEdit, numEdit, info.getId(), pos);
                            }
                        })
                .setNegativeButton(R.string.back,
                        new android.content.DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DlgMgr.cancelDialog(BindedNumInfoListAdapter.this.dialog);
                            }
                        }).create();
        dialog.show();
        DlgMgr.disableDlgDismiss(dialog);
    }

    private void handleClickChangeBtn(final EditText nicknameEdit, final EditText numEdit, int id,
            int pos) {
        String nickName = nicknameEdit.getText().toString();
        String num = numEdit.getText().toString();
        if (Utils.checkPhoneNum(num)) {
            BindedNumInfo info = new BindedNumInfo();
            info.setId(id);
            info.setNickname(nickName);
            info.setPhone_num(num);
            DataMgr.getInstance().updateBindedNumInfo(info);
            infoList.set(pos, info);
            notifyDataSetChanged();
            DlgMgr.cancelDialog(dialog);
        } else {
            Utils.showSingleBtnEventDlg(EventType.PHONE_NUM_INPUT_ERROR, context);
        }
    }

    private class FlagHolder {
        public TextView numView;
        public TextView nicknameView;
        public TextView changeView;
        public TextView deleteView;
    }
}