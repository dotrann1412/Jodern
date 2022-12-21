package com.example.jodern;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.bumptech.glide.request.FutureTarget;

import java.io.File;
import java.util.ArrayList;

public class ShareTask extends AsyncTask<FutureTarget<File>, Void, ArrayList<Uri>> {

    private final Context mContext;
    private final String mMimeType;
    private final String mTitle;

    public ShareTask(Context context, String title, String mimeType) {
        this.mContext = context;
        this.mTitle = title;
        this.mMimeType = mimeType;
    }

    @Override
    protected ArrayList<Uri> doInBackground(FutureTarget<File>... targets) {
        final ArrayList<Uri> imageUris = new ArrayList<>(targets.length);

        for(FutureTarget<File> target : targets) {
            try {
                File file = target.get();
                File jpgFile = new File(file.getParentFile(), file.getName() + ".jpg");
                file.renameTo(jpgFile);

                File imgFile = new File(file.getParentFile(), file.getName() + ".jpg");
                Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", imgFile);
                imageUris.add(uri);
            } catch (Exception ex) {
                Log.w("SHARE", "Sharing failed for one or more image files.", ex);
            }
        }

        return imageUris;
    }

    @Override
    protected void onPostExecute(ArrayList<Uri> result) {
        if(!result.isEmpty()) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, result);
            intent.putExtra(Intent.EXTRA_TEXT, mTitle);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType(mMimeType);
            mContext.startActivity(Intent.createChooser(intent, "Share Your Photo"));
        }
    }
}