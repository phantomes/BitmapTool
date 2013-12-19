package com.washpan.bitmaptool;

import java.util.ArrayList;

import com.washpan.imgtool.BitmapLoader;
import com.washpan.imgtool.BitmapLoader.LoadBitmapListener;
import com.washpan.imgtool.NameUtils;
import com.washpan.imgtool.SafeBitmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class BigImageActivity extends Activity implements OnPageChangeListener {

	public static final String OFFSET = "OFFSET";

	public static void lauchSelft(Context context, int pos) {
		Intent bigIntent = new Intent(context, BigImageActivity.class);
		bigIntent.putExtra(OFFSET, pos);
		context.startActivity(bigIntent);
	}

	private BitmapLoader loader = null;
	private ViewPager pager = null;
	private BigImageAdapter adapter = null;
	private Handler handler = new Handler();
	private Bitmap loadingBitmap = null;

	class MyViewPage {
		public View view;
		public ProgressBar pbLoading;
		public ImageView image;
	}

	private ArrayList<MyViewPage> views = null;
	private int offset = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_big);
		initCache();
		pager = (ViewPager) findViewById(R.id.imagePager);

		views = new ArrayList<BigImageActivity.MyViewPage>(
				TestData.imageUrls.length);
		adapter = new BigImageAdapter();
		views = new ArrayList<MyViewPage>();
		for (int i = 0; i < TestData.imageUrls.length; ++i) {
			LayoutInflater lf = LayoutInflater.from(this);
			View view = lf.inflate(R.layout.big_image_item, null);
			ImageView imageView = (ImageView) view.findViewById(R.id.bigImage);
			ProgressBar pbLoading = (ProgressBar) view.findViewById(R.id.pbloading);
			MyViewPage page = new MyViewPage();
			page.view = view;
			page.image = imageView;
			page.pbLoading = pbLoading;
			views.add(page);
		}
		loadingBitmap = BitmapFactory.decodeResource(
				BigImageActivity.this.getResources(), R.drawable.loading);
		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(this);
		Bundle bundel = getIntent().getExtras();
		if (null != bundel) {
			offset = bundel.getInt(OFFSET, 0);
		}
		pager.setCurrentItem(offset);
		if (0 == offset) {
			onPageSelected(offset);
		}
	}

	class BigImageAdapter extends PagerAdapter {

		public BigImageAdapter() {
		}

		@Override
		public int getCount() {
			return TestData.imageUrls.length;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(views.get(position).view);
			views.get(position).image.setImageBitmap(null);
			// String url = TestData.imageUrls[position];
			// SafeBitmap safeBitmap =
			// loader.getCahe(NameUtils.generateKey(url));
			// if(null !=safeBitmap && null !=safeBitmap.bitmap&&
			// !safeBitmap.bitmap.isRecycled()){
			// safeBitmap.bitmap.recycle();
			// loader.removeCache(NameUtils.generateKey(url));
			// System.runFinalization();
			// }
		}

		@Override
		public Object instantiateItem(View container, int position) {
			ViewPager page = ((ViewPager) container);
			page.removeView(views.get(position).view);
			page.addView(views.get(position).view);
			return views.get(position).view;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

	}

	private void initCache() {
		loader = BitmapLoader.getInstance();
		loader.setHeight(getResources().getDisplayMetrics().heightPixels);
		loader.setWidth(getResources().getDisplayMetrics().widthPixels);
	}

	@Override
	protected void onPause() {
		// if (null != loader) {
		// loader.cleanCache(true);
		// }
		super.onPause();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int pos) {

		final MyViewPage page = views.get(pos);
		String url = TestData.imageUrls[pos];
		page.pbLoading.setVisibility(View.VISIBLE);
		// 加载图片
		loader.loadBitmap(url, new LoadBitmapListener() {

			@Override
			public void onFinish(final SafeBitmap bitmap) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						page.image.setImageBitmap(bitmap.bitmap.get());
						page.pbLoading.setVisibility(View.GONE);
					}
				});
			}

			@Override
			public void onError() {
				handler.post(new Runnable() {

					@Override
					public void run() {
						page.image.setImageBitmap(BitmapFactory.decodeResource(
								BigImageActivity.this.getResources(),
								R.drawable.loading_error));
						page.pbLoading.setVisibility(View.GONE);
					}
				});
			}

		});

	}
}
