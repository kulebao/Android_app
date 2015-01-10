package com.cocobabys.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.cocobabys.R;
import com.cocobabys.adapter.CustomGalleryAdapter;
import com.cocobabys.bean.AblumInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.NoticeAction;
import com.cocobabys.customview.CustomGallery;
import com.cocobabys.fragment.GalleryDirListFragment;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.Utils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

public class CustomGalleryActivity extends BaseEventFragmentActivity {

	private static final String TAG = "CustomGalleryActivity";
	private GridView gridGallery;
	private MyHandler handler;
	private CustomGalleryAdapter adapter;
	private ImageView imgNoMedia;
	private Button btnGalleryOk;
	private SlidingMenu menu;

	// 默认加载加载最近照片100张，其余的通过目录进行选择
	public static final int MAX_PICS_SHOW_IN_GALLERY = 100;
	private static final int UPDATE_UI = 10;

	private String action;
	private String currentDir = ConstantValue.RECENTLY_PIC_DIR;
	private ImageLoader imageLoader;

	// 当前选中的图片，因为图片可以分目录显示，所以需要在这里统一保存一份,adapter里保存的是当前目录下选中的图片
	private ArrayList<CustomGallery> currentSelected = new ArrayList<CustomGallery>(
			10);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_gallery);

		action = getIntent().getAction();
		if (action == null) {
			finish();
			return;
		}
		initHandler();
		initImageLoader();
		createFragmentSlideMenu();
		init();
	}

	private void initHandler() {
		handler = new MyHandler(this) {
			@Override
			public void handleMessage(Message msg) {
				if (CustomGalleryActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case UPDATE_UI:
					adapter.notifyDataSetChanged();
					checkImageStatus();
					break;
				default:
					break;
				}
			}
		};
	}

	private void initImageLoader() {
		imageLoader = ImageUtils.getImageLoader();
	}

	public CustomGalleryAdapter getAdapter() {
		return adapter;
	}

	private void init() {
		initHeader();
		initGallery();
		imgNoMedia = (ImageView) findViewById(R.id.imgNoMedia);
		btnGalleryOk = (Button) findViewById(R.id.btnGalleryOk);
		btnGalleryOk.setOnClickListener(mOkClickListener);

		// 稍作延迟，以免slidemenu还未加载，导致收不到eventbus的消息
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				String[] stringArrayExtra = getIntent().getStringArrayExtra(
						NoticeAction.SELECTED_PATH);
				initCurrentSelected(stringArrayExtra);
				loadData(stringArrayExtra);
				adapter.notifyDataSetChanged();
				checkImageStatus();
			}
		}, 50);
	}

	private void initCurrentSelected(String[] stringArrayExtra) {
		if (stringArrayExtra == null || stringArrayExtra.length == 0) {
			return;
		}
		for (String path : stringArrayExtra) {
			CustomGallery obj = new CustomGallery();
			obj.setSdcardPath(path);
			obj.setSeleted(true);
			currentSelected.add(obj);
		}
	}

	private void initGallery() {
		gridGallery = (GridView) findViewById(R.id.gridGallery);
		gridGallery.setFastScrollEnabled(true);
		adapter = new CustomGalleryAdapter(getApplicationContext(), imageLoader);
		adapter.setShowDefaultPic(true);
		
		if (action.equalsIgnoreCase(NoticeAction.ACTION_MULTIPLE_PICK)) {
			findViewById(R.id.llBottomContainer).setVisibility(View.VISIBLE);
			gridGallery.setOnItemClickListener(mItemMulClickListener);
			adapter.setMultiplePick(true);
		} else if (action.equalsIgnoreCase(NoticeAction.ACTION_PICK)) {
			findViewById(R.id.llBottomContainer).setVisibility(View.GONE);
			gridGallery.setOnItemClickListener(mItemSingleClickListener);
			adapter.setMultiplePick(false);
		}

		gridGallery.setAdapter(adapter);
	}

	private void initHeader() {
		Button showSlideMenu = (Button) findViewById(R.id.leftBtn);
		showSlideMenu.setText(R.string.album);
		showSlideMenu.setVisibility(View.VISIBLE);
		showSlideMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (adapter != null) {
					menu.showMenu();
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (menu.isMenuShowing()) {
			menu.showContent();
		} else {
			super.onBackPressed();
		}
	}

	private void loadData(final String[] selected_path) {
		ArrayList<CustomGallery> galleryPhotos = null;
		adapter.clear();

		if (ConstantValue.RECENTLY_PIC_DIR.equals(currentDir)) {
			galleryPhotos = DataUtils.getRecentlyGalleryPhotos();
		} else {
			galleryPhotos = DataUtils.getGalleryPhotosByDir(currentDir);
		}

		adapter.addAll(galleryPhotos);
		adapter.setSelected(selected_path);
		postFirstColumnDataToFragment();
	}

	// 将已经获取到的数据发送给fragment，显示在列表第一行，避免重复获取数据
	private void postFirstColumnDataToFragment() {
		AblumInfo firstInfo = new AblumInfo();
		firstInfo.setDirName(getResources().getString(R.string.recent_photo));
		firstInfo.setDirCount(adapter.getCount());
		if (adapter.getCount() > 0) {
			firstInfo.setLastestPicPath("file://"
					+ adapter.getData().get(0).getSdcardPath());
		}

		EventBus.getDefault().post(firstInfo);
	}

	private void checkImageStatus() {
		if (adapter.isEmpty()) {
			imgNoMedia.setVisibility(View.VISIBLE);
		} else {
			imgNoMedia.setVisibility(View.GONE);
		}
	}

	public void onEventBackgroundThread(String dirName) {
		// 如果用户选择了不同的目录，需要重新加载数据
		if (!currentDir.equals(dirName)) {
			Log.d("DDD", TAG + "onEventBackgroundThread 222 dirName=" + dirName
					+ " currentDir=" + currentDir);
			currentDir = dirName;
			// String[] selected = formatSelected(getCurrentSelected());
			loadData(formatSelected(currentSelected));
			handler.sendEmptyMessage(UPDATE_UI);
		} else {
			Log.d("DDD", TAG + "onEventBackgroundThread 222 dirName=" + dirName);
		}
	}

	private String[] formatSelected(ArrayList<CustomGallery> selected) {
		String[] allPath = new String[selected.size()];

		for (int i = 0; i < allPath.length; i++) {
			allPath[i] = selected.get(i).getSdcardPath();
		}
		return allPath;
	}

	private View.OnClickListener mOkClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (currentSelected.isEmpty()) {
				Utils.makeToast(CustomGalleryActivity.this, R.string.choose_pic);
				return;
			}

			String[] allPath = formatSelected(currentSelected);

			Intent data = new Intent().putExtra(NoticeAction.ALL_PATH, allPath);
			setResult(RESULT_OK, data);
			finish();
		}
	};

	public void changeSelection(View v, List<CustomGallery> data, int position) {
		if (data.get(position).isSeleted()) {
			data.get(position).setSeleted(false);
		} else {
			if (checkMaxIconSelected()) {
				return;
			}
			data.get(position).setSeleted(true);
		}

		adapter.changeSelection(v, position);
		changeCurrentSelected(position);
	}

	private boolean checkMaxIconSelected() {
		if (currentSelected.size() >= ConstantValue.MAX_SELECT_LIMIT) {
			String content = String.format(
					Utils.getResString(R.string.max_icon_select),
					ConstantValue.MAX_SELECT_LIMIT);
			Utils.makeToast(CustomGalleryActivity.this, content);
			return true;
		}

		return false;
	}

	private AdapterView.OnItemClickListener mItemMulClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			changeSelection(v, adapter.getData(), position);
		}
	};

	private void changeCurrentSelected(int position) {
		CustomGallery item = adapter.getItem(position);
		boolean seleted = item.isSeleted();

		if (!currentSelected.contains(item) && seleted) {
			currentSelected.add(item);
			return;
		}

		if (currentSelected.contains(item) && !seleted) {
			currentSelected.remove(item);
			return;
		}
	}

	private AdapterView.OnItemClickListener mItemSingleClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			CustomGallery item = adapter.getItem(position);
			Intent data = new Intent().putExtra("single_path",
					item.getSdcardPath());
			setResult(RESULT_OK, data);
			finish();
		}
	};

	private void createFragmentSlideMenu() {
		// configure the SlidingMenu
		menu = new SlidingMenu(this);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_no_offset);
		menu.setBehindScrollScale(0.25f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new GalleryDirListFragment())
				.commit();
	}

	@Override
	protected void onDestroy() {
		adapter.clearCache();
		super.onDestroy();
	}

}
