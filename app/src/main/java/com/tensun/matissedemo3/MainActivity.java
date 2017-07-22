package com.tensun.matissedemo3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE = 886;                                                             // 自定義參數

    private UriAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter = new UriAdapter());

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            Matisse.from(MainActivity.this)                                                             // 傳入一個Activity作為參數, 返回一個Matisse實例
                                    .choose(MimeType.ofAll(), false)                                                     // 允許選擇所有格式
                                    .countable(true)                                                                    // 關於右上角的小圓圈, true 為顯示數字, flase 為顯示打勾
                                    .capture(false)                                                                     // 是否在Grid佈局中 添加拍照功能
                                    .captureStrategy(new CaptureStrategy(true, "capture"))                              // 設置保存圖片權限策略
                                    .maxSelectable(100)                                                                 // 圖片選擇的最大數量
                                    .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))                   // 添加過濾器, 對於寬高不足320, 或體積大於5M 的GIF圖片做出攔截, 並告訴用戶為什麼不能選
                                    .gridExpectedSize(                                                                  // 設置Grid佈局中圖片的寬度
                                            getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                                    .theme(R.style.Free)                                                                 // 自定義主題配色
                                    .showSingleMediaType(true)
                                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)                  // 設定圖片選擇器Activity的顯示方向為直的
                                    .thumbnailScale(0.85f)                                                              // 縮略圖的壓縮值, 0.0f ~ 1.0f, 0.85f 表示壓縮成原來的85%
                                    .imageEngine(new GlideEngine())                                                     // 設置圖片加載引擎, 1. GlideEngine()  2. PicassoEngine()  3. 自定義
                                    .forResult(REQUEST_CODE_CHOOSE);                                                // 調用這個方法 可以進入圖片選擇框架

                            mAdapter.setData(null, null);
                        } else {
                            Toast.makeText(
                                    MainActivity.this, R.string.permission_request_denied, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /** 接收回調, 因為 forResult() 內部調用了startActivityForResult */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {                                     // 從MatisseActivity 跳轉回原來的Activity時, 會調用此方法
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mAdapter.setData(Matisse.obtainResult(data), Matisse.obtainPathResult(data));                                    // 將資料賦予mAdapter
        }
    }

    private static class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

        private List<Uri> mUris;
        private List<String> mPaths;

        void setData(List<Uri> uris, List<String> paths) {
            mUris = uris;
            mPaths = paths;
            notifyDataSetChanged();
        }

        @Override
        public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UriViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false));
        }

        @Override
        public void onBindViewHolder(UriViewHolder holder, int position) {
            holder.mUri.setText(mUris.get(position).toString());
            holder.mPath.setText(mPaths.get(position));

            holder.mUri.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
            holder.mPath.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
        }

        @Override
        public int getItemCount() {
            return mUris == null ? 0 : mUris.size();
        }

        static class UriViewHolder extends RecyclerView.ViewHolder {

            private TextView mUri;
            private TextView mPath;

            UriViewHolder(View contentView) {
                super(contentView);
                mUri = (TextView) contentView.findViewById(R.id.uri);
                mPath = (TextView) contentView.findViewById(R.id.path);
            }
        }
    }
}
