package com.example.fragment;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.CanChatUdpReceiver;
import util.ListContactInfo;

import com.example.found.MainChatActivity;
import com.example.found.R;
import com.example.view.NoScrollListView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
public class ListFragment extends Fragment implements OnTouchListener{

	private final int UPDATE_CONTACTLIST = 0;

	private NoScrollListView myListView;

	private ImageView unNamed;

	private int[] imageIds = new int[] { R.drawable.dml, R.drawable.dmk,
			R.drawable.dmn, R.drawable.dmm };

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == UPDATE_CONTACTLIST) {
				updateMyListView();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 开启线程实时更新列表
		new Thread() {
			public void run() {
				try {
					while (true) {
						Thread.sleep(10000);// 每10秒更新一次
						Message msg = new Message();
						msg.what = UPDATE_CONTACTLIST;
						handler.sendMessage(msg);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_list, container, false);

		myListView = (NoScrollListView) view.findViewById(R.id.listPeople);

		unNamed = (ImageView) view.findViewById(R.id.unNamed);

		// 初始化联系人列表
		updateMyListView();

		// 联系人列表点击设置

		myListView
				.setOnItemClickListener(new NoScrollListView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						Intent intent = new Intent(getActivity(),
								MainChatActivity.class);

						TextView listUserName = (TextView) view
								.findViewById(R.id.listUserName);
						TextView listUserIp = (TextView) view
								.findViewById(R.id.listUserIp);
						String listName = listUserName.getText().toString();
						String listIp = listUserIp.getText().toString();
						intent.putExtra("listName", listName);// 传递列表名
						intent.putExtra("listIp", listIp);// 传递ip

						startActivity(intent);

					}
				});

		unNamed.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateMyListView();
			}
		});
		return view;
	}

	// 更新联系人列表 ，列表数据处理
	private List<Map<String, Object>> getData(
			List<ListContactInfo> listContactInfos) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		long time = System.currentTimeMillis();
		for (int i = 0; i < listContactInfos.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();

			ListContactInfo info = listContactInfos.get(i);

			int listImage = info.getListImage();
			String listIp = info.getListIp();
			String listName = info.getListName();
			String listTime = info.getListTime();
			String listUnRead = info.getListUnRead();

			map.put("listUserIp", listIp);
			map.put("listUserName", listName);
			if (i == 0) {
				map.put("headImage", R.drawable.lfp);
			} else {
				map.put("headImage", listImage);
			}
			map.put("listTime", listTime);
			map.put("unReadcounts", listUnRead);

			list.add(map);
		}
		return list;
	}

	// 更新联系人列表
	public void updateMyListView() {
		try {
			if (CanChatUdpReceiver.listContactInfos != null) {
				SimpleAdapter adapter = new SimpleAdapter(getActivity(),
						getData(CanChatUdpReceiver.listContactInfos),
						R.layout.item_list_people, new String[] {
								"listUserName", "listUserIp", "headImage",
								"listTime", "unReadcounts" }, new int[] {
								R.id.listUserName, R.id.listUserIp,
								R.id.headImage, R.id.listTime,
								R.id.unReadcounts });
				myListView.setAdapter(adapter);
			} else {
				Log.e("错误提示：", "列表无数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// 更新联系人列表
		updateMyListView();
	}

	// 手指按下时的屏幕纵坐标
	private float yDown;
	// 当前是否可以下拉，只有ListView滚动到头的时候才允许下拉
	private boolean ableToPull;
	// 下拉头的View
	private View header;
	// 下拉状态
	public static final int STATUS_PULL_TO_REFRESH = 0;
	// 释放立即刷新状态
	public static final int STATUS_RELEASE_TO_REFRESH = 1;
	// 正在刷新状态
	public static final int STATUS_REFRESHING = 2;
	// 刷新完成或未刷新状态
	public static final int STATUS_REFRESH_FINISHED = 3;
	// 下拉头部回滚的速度
	public static final int SCROLL_SPEED = -20;
	// 刷新时显示的进度条
	private ProgressBar progressBar;
	// 用于存储上次更新时间
	private SharedPreferences preferences;
	// 指示下拉和释放的箭头
	private ImageView arrow;
	// 指示下拉和释放的文字描述
	private TextView description;
	// 上次更新时间的文字描述
	private TextView updateAt;
	// 在被判定为滚动之前用户手指可以移动的最大值。
	private int touchSlop;
	// 是否已加载过一次layout，这里onLayout中的初始化只需加载一次
	private boolean loadOnce;
	// 下拉头的布局参数
	private MarginLayoutParams headerLayoutParams;
	// 下拉头的高度
	private int hideHeaderHeight;
	/*
	 * 当前处理什么状态，可选值有STATUS_PULL_TO_REFRESH, STATUS_RELEASE_TO_REFRESH,
	 * STATUS_REFRESHING 和 STATUS_REFRESH_FINISHED
	 */
	private int currentStatus = STATUS_REFRESH_FINISHED;;
	// 记录上一次的状态是什么，避免进行重复操作
	private int lastStatus = currentStatus;
	// 下拉刷新的回调接口
	private PullToRefreshListener mListener;
	// 上次更新时间的毫秒值
	private long lastUpdateTime;
	// 一分钟的毫秒值，用于判断上次的更新时间
	public static final long ONE_MINUTE = 60 * 1000;
	// 一小时的毫秒值，用于判断上次的更新时间
	public static final long ONE_HOUR = 60 * ONE_MINUTE;
	// 一天的毫秒值，用于判断上次的更新时间
	public static final long ONE_DAY = 24 * ONE_HOUR;
	// 一月的毫秒值，用于判断上次的更新时间
	public static final long ONE_MONTH = 30 * ONE_DAY;
	// 一年的毫秒值，用于判断上次的更新时间
	public static final long ONE_YEAR = 12 * ONE_MONTH;
	// 上次更新时间的字符串常量，用于作为SharedPreferences的键值
	private static final String UPDATED_AT = "updated_at";
	// 为了防止不同界面的下拉刷新在上次更新时间上互相有冲突，使用id来做区分
	private int mId = -1;

	/**
	 * 下拉刷新控件的构造函数，会在运行时动态添加一个下拉头的布局。
	 * 
	 * @param context
	 * @param attrs
	 */

	public void RefreshableView(Context context, AttributeSet attrs) {
		// super(context, attrs);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		header = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh,
				null, true);
		progressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
		arrow = (ImageView) header.findViewById(R.id.arrow);
		description = (TextView) header.findViewById(R.id.description);
		updateAt = (TextView) header.findViewById(R.id.updated_at);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		// refreshUpdatedAtValue();
		// setOrientation(VERTICAL);
		// addView(header, 0);
	}

	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		setIsAbleToPull(event);
		if (ableToPull) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				yDown = event.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				float yMove = event.getRawY();
				int distance = (int) (yMove - yDown);
				// 如果手指是下滑状态，并且下拉头是完全隐藏的，就屏蔽下拉事件
				if (distance <= 0
						&& headerLayoutParams.topMargin <= hideHeaderHeight) {
					return false;
				}
				if (distance < touchSlop) {
					return false;
				}
				if (currentStatus != STATUS_REFRESHING) {
					if (headerLayoutParams.topMargin > 0) {
						currentStatus = STATUS_RELEASE_TO_REFRESH;
					} else {
						currentStatus = STATUS_PULL_TO_REFRESH;
					}
					// 通过偏移下拉头的topMargin值，来实现下拉效果
					headerLayoutParams.topMargin = (distance / 2)
							+ hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
				}
				break;
			case MotionEvent.ACTION_UP:
			default:
				if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
					// 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
					new RefreshingTask().execute();
				} else if (currentStatus == STATUS_PULL_TO_REFRESH) {
					// 松手时如果是下拉状态，就去调用隐藏下拉头的任务
					new HideHeaderTask().execute();
				}
				break;
			}
			// 时刻记得更新下拉头中的信息
			if (currentStatus == STATUS_PULL_TO_REFRESH
					|| currentStatus == STATUS_RELEASE_TO_REFRESH) {
				updateHeaderView();
				// 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
				myListView.setPressed(false);
				myListView.setFocusable(false);
				myListView.setFocusableInTouchMode(false);
				lastStatus = currentStatus;
				// 当前正处于下拉或释放状态，通过返回true屏蔽掉ListView的滚动事件
				return true;
			}
		}
		return false;
	}

	/**
	 * 根据当前ListView的滚动状态来设定 {@link #ableToPull}
	 * 的值，每次都需要在onTouch中第一个执行，这样可以判断出当前应该是滚动ListView，还是应该进行下拉。
	 * 
	 */
	private void setIsAbleToPull(MotionEvent event) {
		View firstChild = myListView.getChildAt(0);
		if (firstChild != null) {
			int firstVisiblePos = myListView.getFirstVisiblePosition();
			if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
				if (!ableToPull) {
					yDown = event.getRawY();
				}
				// 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
				ableToPull = true;
			} else {
				if (headerLayoutParams.topMargin != hideHeaderHeight) {
					headerLayoutParams.topMargin = hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
				}
				ableToPull = false;
			}
		} else {
			// 如果ListView中没有元素，也应该允许下拉刷新
			ableToPull = true;
		}
	}

	/**
	 * 更新下拉头中的信息。
	 */
	private void updateHeaderView() {
		if (lastStatus != currentStatus) {
			if (currentStatus == STATUS_PULL_TO_REFRESH) {
				description.setText(getResources().getString(
						R.string.pull_to_refresh));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
				description.setText(getResources().getString(
						R.string.release_to_refresh));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_REFRESHING) {
				description.setText(getResources().getString(
						R.string.refreshing));
				progressBar.setVisibility(View.VISIBLE);
				arrow.clearAnimation();
				arrow.setVisibility(View.GONE);
			}
			refreshUpdatedAtValue();
		}
	}

	/**
	 * 根据当前的状态来旋转箭头。
	 */
	private void rotateArrow() {
		float pivotX = arrow.getWidth() / 2f;
		float pivotY = arrow.getHeight() / 2f;
		float fromDegrees = 0f;
		float toDegrees = 0f;
		if (currentStatus == STATUS_PULL_TO_REFRESH) {
			fromDegrees = 180f;
			toDegrees = 360f;
		} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
			fromDegrees = 0f;
			toDegrees = 180f;
		}
		RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees,
				pivotX, pivotY);
		animation.setDuration(100);
		animation.setFillAfter(true);
		arrow.startAnimation(animation);
	}

	/**
	 * 刷新下拉头中上次更新时间的文字描述。
	 */
	private void refreshUpdatedAtValue() {
		lastUpdateTime = preferences.getLong(UPDATED_AT + mId, -1);
		long currentTime = System.currentTimeMillis();
		long timePassed = currentTime - lastUpdateTime;
		long timeIntoFormat;
		String updateAtValue;
		if (lastUpdateTime == -1) {
			updateAtValue = getResources().getString(R.string.not_updated_yet);
		} else if (timePassed < 0) {
			updateAtValue = getResources().getString(R.string.time_error);
		} else if (timePassed < ONE_MINUTE) {
			updateAtValue = getResources().getString(R.string.updated_just_now);
		} else if (timePassed < ONE_HOUR) {
			timeIntoFormat = timePassed / ONE_MINUTE;
			String value = timeIntoFormat + "分钟";
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_DAY) {
			timeIntoFormat = timePassed / ONE_HOUR;
			String value = timeIntoFormat + "小时";
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_MONTH) {
			timeIntoFormat = timePassed / ONE_DAY;
			String value = timeIntoFormat + "天";
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_YEAR) {
			timeIntoFormat = timePassed / ONE_MONTH;
			String value = timeIntoFormat + "个月";
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		} else {
			timeIntoFormat = timePassed / ONE_YEAR;
			String value = timeIntoFormat + "年";
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		}
		updateAt.setText(updateAtValue);
	}

	/**
	 * 正在刷新的任务，在此任务中会去回调注册进来的下拉刷新监听器。
	 * 
	 */
	class RefreshingTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			int topMargin = headerLayoutParams.topMargin;
			while (true) {
				topMargin = topMargin + SCROLL_SPEED;
				if (topMargin <= 0) {
					topMargin = 0;
					break;
				}
				publishProgress(topMargin);
				sleep(10);
			}
			currentStatus = STATUS_REFRESHING;
			publishProgress(0);
			if (mListener != null) {
				mListener.onRefresh();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... topMargin) {
			updateHeaderView();
			headerLayoutParams.topMargin = topMargin[0];
			header.setLayoutParams(headerLayoutParams);
		}
	}

	/**
	 * 隐藏下拉头的任务，当未进行下拉刷新或下拉刷新完成后，此任务将会使下拉头重新隐藏。
	 * 
	 */
	class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			int topMargin = headerLayoutParams.topMargin;
			while (true) {
				topMargin = topMargin + SCROLL_SPEED;
				if (topMargin <= hideHeaderHeight) {
					topMargin = hideHeaderHeight;
					break;
				}
				publishProgress(topMargin);
				sleep(10);
			}
			return topMargin;
		}
	}

	/**
	 * 使当前线程睡眠指定的毫秒数。
	 * 
	 * @param time
	 *            指定当前线程睡眠多久，以毫秒为单位
	 */
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
	 * 
	 * @author guolin
	 */
	public interface PullToRefreshListener {
		/**
		 * 刷新时会去回调此方法，在方法内编写具体的刷新逻辑。注意此方法是在子线程中调用的， 你可以不必另开线程来进行耗时操作。
		 */
		void onRefresh();
	}
}
