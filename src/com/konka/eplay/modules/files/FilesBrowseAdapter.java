package com.konka.eplay.modules.files;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konka.eplay.Constant;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.AsyncImageView;
import com.konka.eplay.modules.CommonFileInfo;

import java.util.List;


public class FilesBrowseAdapter extends BaseAdapter {
    private Context mContext;
    private List<CommonFileInfo> mFileList;
    private LayoutInflater inflater;

    public FilesBrowseAdapter(Context context, List<CommonFileInfo> fileList) {
        mContext = context;
        mFileList = fileList;
        inflater = LayoutInflater.from(mContext);
    }


    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.file_browser_adpter, null);
            holder.linearLayout = (RelativeLayout) convertView
                    .findViewById(R.id.file_bottom);
            holder.textView = (TextView) convertView
                    .findViewById(R.id.file_name);
            holder.imageView = (AsyncImageView) convertView
                    .findViewById(R.id.file_image);
            holder.iconImage = (ImageView) convertView
                    .findViewById(R.id.file_mmt_icon);
            holder.sizeView = (TextView) convertView
                    .findViewById(R.id.file_size);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mContext instanceof FilesActivity && ((FilesActivity)mContext).getGridView().isOnMeasure()) {
            return convertView;
        }

        CommonFileInfo fileInfo = mFileList.get(position);

        String path = fileInfo.getPath();
        String name = fileInfo.getName();

        holder.textView.setText(name);

        holder.imageView.setImageBitmap(null);
        holder.imageView.setPath(path, false);



//        if (Utils.getMmt(path) == Constant.MultimediaType.MMT_PHOTO) {
//            holder.iconImage.setImageResource(R.drawable.photo_default_sml);
//
//        } else if (Utils.getMmt(path) == Constant.MultimediaType.MMT_MOVIE) {
//            holder.iconImage.setImageResource(R.drawable.video_default_sml);
//        } else if (Utils.getMmt(path) == Constant.MultimediaType.MMT_MUSIC) {
//            holder.iconImage.setImageResource(R.drawable.music_default_sml);
//        }else if (name.endsWith(".ppt")||name.endsWith(".pptx")) {
//            holder.iconImage.setImageResource(R.drawable.com_ppt_icon);
//        }
//        else if (name.endsWith(".doc")||name.endsWith(".docx")){
//            holder.iconImage.setImageResource(R.drawable.com_word_icon);
//        } else if (name.endsWith(".xls")) {
//            holder.iconImage.setImageResource(R.drawable.com_excel_icon);
//        } else if (name.endsWith(".pdf")) {
//            holder.iconImage.setImageResource(R.drawable.com_pdf_sml);
//        }else if (name.endsWith(".txt")) {
//            holder.iconImage.setImageResource(R.drawable.com_txt_sml);
//        }else if (Utils.getMmt(path)== Constant.MultimediaType.MMT_ARCHIVE) {
//            holder.iconImage.setImageResource(R.drawable.com_zip_sml);
//        }
//        else if (Utils.getMmt(path)== Constant.MultimediaType.MMT_APK) {
//            holder.iconImage.setImageResource(R.drawable.com_zip_sml);
//        } else if (fileInfo.isDir()) {
//            holder.iconImage.setImageResource(R.drawable.com_folder_unfocus);
//        } else {
//            holder.iconImage.setImageResource(R.drawable.item_default_sml);
//        }


        if (fileInfo.isDir()) {
            int childrenCount = fileInfo.getChildrenCount();
            if (childrenCount >= 0) {
                holder.sizeView.setText("" + childrenCount);
            }

        } else {
            holder.sizeView.setText(null);

        }

        return convertView;
    }

    class ViewHolder {
        RelativeLayout linearLayout;
        AsyncImageView imageView;
        TextView textView;
        ImageView iconImage;
        TextView sizeView;
    }

}
