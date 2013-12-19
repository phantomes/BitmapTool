package com.washpan.bitmaptool;

import java.io.File;

import com.washpan.imgtool.BitmapLoader;
import com.washpan.imgtool.BitmapLoader.LoadBitmapListener;
import com.washpan.imgtool.NameUtils;
import com.washpan.imgtool.SafeBitmap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;


public class ImagesListActivity extends Activity {

	public static final String PATH = "/safeimages/";
	public static String STORE_PATH = "";

	static {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			File storeFile = Environment.getExternalStorageDirectory();
			String basePath = storeFile.getPath();
			File destDir = new File(basePath, PATH);
			STORE_PATH = destDir.getPath();
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
		}
	}

	private ListView lvImages = null;

	private ImageCaheAdapter adapter = null;
	private Handler handler = new Handler();
	private BitmapLoader loader = null;
	private Bitmap bitmapLoading = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		lvImages = (ListView) findViewById(R.id.lvList);
		// 初始化缓存,大小可以按照自己需要进行调节
		initCache();
		adapter = new ImageCaheAdapter();
		lvImages.setAdapter(adapter);
		lvImages.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				BigImageActivity.lauchSelft(ImagesListActivity.this, pos);
			}
		});
		bitmapLoading = BitmapFactory.decodeResource(
				ImagesListActivity.this.getResources(),
				R.drawable.loading);
	}

	private void initCache() {
		int cacheSize = Math.round(Runtime.getRuntime().maxMemory() >> 12);
		loader = BitmapLoader.getInstance().init(cacheSize, STORE_PATH, getResources()
				.getDisplayMetrics().widthPixels, 100);
	}

	class ImageCaheAdapter extends BaseAdapter {
		public class ImageHolder {
			public ImageView image;
			public ProgressBar pbLoading;
		}

		@Override
		public int getCount() {
			return TestData.imageUrls.length;
		}

		@Override
		public Object getItem(int position) {
			return TestData.imageUrls[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ImageHolder();
				convertView = LayoutInflater.from(
						ImagesListActivity.this.getApplicationContext())
						.inflate(R.layout.image_list_item, null);
				viewHolder.image = (ImageView) convertView
						.findViewById(R.id.ivItem);
				viewHolder.pbLoading = (ProgressBar) convertView
						.findViewById(R.id.pbItem);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ImageHolder) convertView.getTag();
			}
			viewHolder.image.setImageBitmap(null);
			viewHolder.pbLoading.setVisibility(View.VISIBLE);
			
			String url = TestData.imageUrls[position];
			SafeBitmap safeBitmap = loader.getCahe(NameUtils.generateKey(url));
			final ImageHolder imgHolder = viewHolder;
			// 如果cache有直接加载cache
			if (null != safeBitmap && null != safeBitmap.bitmap && null != safeBitmap.bitmap.get()) {
				viewHolder.image.setImageBitmap(safeBitmap.bitmap.get());
				viewHolder.pbLoading.setVisibility(View.GONE);
			} else {
				// 加载图片
				loader.loadBitmap(
						TestData.imageUrls[position], new LoadBitmapListener() {

							@Override
							public void onFinish(
									final SafeBitmap bitmap) {
								handler.post(new Runnable() {

									@Override
									public void run() {

										imgHolder.image.setImageBitmap(bitmap.bitmap.get());
										imgHolder.pbLoading.setVisibility(View.GONE);
									}
								});
							}

							@Override
							public void onError() {
								handler.post(new Runnable() {

									@Override
									public void run() {
										imgHolder.image.setImageBitmap(BitmapFactory.decodeResource(
												ImagesListActivity.this
														.getResources(),
												R.drawable.loading_error));
										imgHolder.pbLoading.setVisibility(View.GONE);
									}
								});

							}

						});
			}
			return convertView;
		}

	}

	@Override
	protected void onPause() {
		 //这里我们最好要清掉缓存(非强制,只是为了速度更快),但不设置回收标志位
		if (null != loader) {
			loader.cleanCache(false);
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		// 这里我们重新设置缓存
		initCache();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// 清掉缓存并设置标志位
		if (null != loader) {
			loader.cleanCache(true);
		}
		super.onDestroy();
	}

}
