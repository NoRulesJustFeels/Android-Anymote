/*
 * Copyright (C) 2012 Google Inc.  All rights reserved.
 * Copyright (C) 2012 ENTERTAILION, LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.entertailion.android.anymote;

import java.net.Inet4Address;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.entertailion.java.anymote.client.DeviceSelectListener;
import com.entertailion.java.anymote.connection.TvDevice;

/**
 * Used to present the discovered Google TV devices to the user. When user
 * selects one of the listed devices from this dialog, connection to the device
 * is initiated.
 */
public class DeviceSelectDialog extends Dialog {
	private List<TvDevice> tvDevices;
	private TvDevice currentDevice;
    private DeviceSelectListener listener;

    /**
     * Constructor.
     * 
     * @param context owner of the dialog.
     */
    public DeviceSelectDialog(Context context, List<TvDevice> tvDevices, TvDevice currentDevice, DeviceSelectListener listener) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        
        this.tvDevices = tvDevices;
        this.currentDevice = currentDevice;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ListView deviceView;

        setContentView(R.layout.device_select_layout);

        deviceView = (ListView) findViewById(R.id.dsl_stb_list);
        deviceView.setAdapter(new DeviceListAdapter(tvDevices));

        deviceView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TvDevice TvDevice = (TvDevice) parent.getItemAtPosition(position);
                selectDevice(TvDevice);
            }
        });

        findViewById(R.id.dsl_manual).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showManualIpDialog();
            }
        });

        setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                selectDevice(null);
            }
        });

        setCancelable(true);
        super.onCreate(savedInstanceState);
    }

    /**
     * Represents an entry in the box list.
     */
    private static class ListEntryView extends LinearLayout {

        private Context context = null;
        private TvDevice listEntry = null;
        private TextView tvName = null;
        private TextView tvTargetAddr = null;

        public ListEntryView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.context = context;
        }

        public ListEntryView(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            tvName = (TextView) findViewById(R.id.device_select_item_name);
            tvTargetAddr = (TextView) findViewById(R.id.device_select_item_address);
        }

        private void updateContents() {
            if (null != tvName) {
                String txt = context.getString(R.string.unkown_tgt_name);
                if ((null != listEntry) && (null != listEntry.getName())) {
                    txt = listEntry.getName();
                }
                tvName.setText(txt);
            }

            if (null != tvTargetAddr) {
                String txt = context.getString(R.string.unkown_tgt_addr);
                if ((null != listEntry) && (null != listEntry.getLocation())) {
                    txt = listEntry.getLocation();
                }
                tvTargetAddr.setText(txt);
            }
        }

        public void setListEntry(TvDevice listEntry) {
            this.listEntry = listEntry;
            updateContents();
        }

    }

    /**
     * Internal listview adapter.
     */
    private class DeviceListAdapter extends BaseAdapter {
        private final List<TvDevice> trackedDevices;

        public DeviceListAdapter(List<TvDevice> trackedDevices) {
            this.trackedDevices = trackedDevices;
            setRecentDevices(null);
        }

        public int getCount() {
            return getTotalSize();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return (position != 0);
        }

        public Object getItem(int position) {
            return getTvDevice(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public void setRecentDevices(TvDevice[] devices) {

            notifyDataSetChanged();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == 0) {
                return getHeaderView(R.string.finder_connect, convertView);
            }
            // Skip Discovered Devices header.
            position -= 1;

            if (position < trackedDevices.size()) {
                return getDeviceView(trackedDevices.get(position), convertView, false);
            }
            // Nothing more to skip, invalid index.
            return null;
        }

        /**
         * Construct titled separator for listview.
         * 
         * @param resource string resource identifying title.
         * @param convertView currently presented view.
         * @return separator view.
         */
        private View getHeaderView(int resource, View convertView) {
            View view = getLayoutInflater().inflate(R.layout.device_select_item_separator_layout,
                    null);
            TextView text = (TextView) view.findViewById(R.id.header_text);
            text.setText(resource);
            return view;
        }

        /**
         * Construct view representing device.
         * 
         * @param device element associated with the view.
         * @param convertView currently presented view.
         * @param isRecent specifies whether device has been recently connected.
         * @return device item.
         */
        private View getDeviceView(TvDevice device, View convertView, boolean isRecent) {
            ListEntryView itemView;
            ImageView image;

            if (convertView == null || !(convertView instanceof ListEntryView)) {
                itemView = (ListEntryView) getLayoutInflater().inflate(
                        R.layout.device_select_item_layout, null);
            } else {
                itemView = (ListEntryView) convertView;
            }

            image = (ImageView) itemView.findViewById(R.id.device_select_item_image);
            if (image != null) {
                if (device.equals(currentDevice)) {
                    image.setImageResource(android.R.drawable.ic_menu_upload_you_tube);
                } else if (isRecent) {
                    image.setImageResource(android.R.drawable.ic_menu_recent_history);
                } else {
                    image.setImageResource(android.R.drawable.ic_menu_search);
                }
            }

            if (device.equals(currentDevice)) {
                itemView.setBackgroundColor(Color.argb(0x80, 0x00, 0x40, 0x20));
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            itemView.setListEntry(device);
            return itemView;
        }

        private int getTotalSize() {
            // return sum of tracked devices and header.
            return trackedDevices.size() + 1;
        }

        private TvDevice getTvDevice(int position) {
            // Skip header.
            position--;
            if (position < 0) {
                return null;
            }

            // and check, if the new position fits into the second group.
            if (position < trackedDevices.size()) {
                return trackedDevices.get(position);
            }

            // bad luck.
            return null;
        }

        public void setCurrentDevice(TvDevice device) {
            currentDevice = device;
            notifyDataSetChanged();
        }
    }

    /**
     * Present dialog allowing manual IP address specification.
     */
    private void showManualIpDialog() {
        DeviceManualIPDialog ipDialog = new DeviceManualIPDialog(getContext());
        ipDialog.setManualIPListener(new DeviceManualIPDialog.ManualIPListener() {
            public void onSelect(String name, Inet4Address address, int port) {
                selectDevice(new TvDevice(name, address, port));
            }

            public void onCancel() {
            }
        });
        ipDialog.show();
    }

    /**
     * Connect to specified device.
     * 
     * @param device target device.
     */
    private void selectDevice(TvDevice device) {
        if (listener != null) {
            if (device != null) {
            	listener.onDeviceSelected(device);
            } else {
            	listener.onDeviceSelectCancelled();
            }
        }
        dismiss();
    }

    /**
     * Set object receiving device select events.
     * 
     * @param listener target object.
     */
    public void setDeviceSelectListener(DeviceSelectListener listener) {
        this.listener = listener;
    }
}
