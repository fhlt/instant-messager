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

		// �����߳�ʵʱ�����б�
		new Thread() {
			public void run() {
				try {
					while (true) {
						Thread.sleep(10000);// ÿ10�����һ��
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

		// ��ʼ����ϵ���б�
		updateMyListView();

		// ��ϵ���б�������

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
						intent.putExtra("listName", listName);// �����б���
						intent.putExtra("listIp", listIp);// ����ip

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

	// ������ϵ���б� ���б����ݴ���
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

	// ������ϵ���б�
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
				Log.e("������ʾ��", "�б�������");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// ������ϵ���б�
		updateMyListView();
	}

	// ��ָ����ʱ����Ļ������
	private float yDown;
	// ��ǰ�Ƿ����������ֻ��ListView������ͷ��ʱ�����������
	private boolean ableToPull;
	// ����ͷ��View
	private View header;
	// ����״̬
	public static final int STATUS_PULL_TO_REFRESH = 0;
	// �ͷ�����ˢ��״̬
	public static final int STATUS_RELEASE_TO_REFRESH = 1;
	// ����ˢ��״̬
	public static final int STATUS_REFRESHING = 2;
	// ˢ����ɻ�δˢ��״̬
	public static final int STATUS_REFRESH_FINISHED = 3;
	// ����ͷ���ع����ٶ�
	public static final int SCROLL_SPEED = -20;
	// ˢ��ʱ��ʾ�Ľ�����
	private ProgressBar progressBar;
	// ���ڴ洢�ϴθ���ʱ��
	private SharedPreferences preferences;
	// ָʾ�������ͷŵļ�ͷ
	private ImageView arrow;
	// ָʾ�������ͷŵ���������
	private TextView description;
	// �ϴθ���ʱ�����������
	private TextView updateAt;
	// �ڱ��ж�Ϊ����֮ǰ�û���ָ�����ƶ������ֵ��
	private int touchSlop;
	// �Ƿ��Ѽ��ع�һ��layout������onLayout�еĳ�ʼ��ֻ�����һ��
	private boolean loadOnce;
	// ����ͷ�Ĳ��ֲ���
	private MarginLayoutParams headerLayoutParams;
	// ����ͷ�ĸ߶�
	private int hideHeaderHeight;
	/*
	 * ��ǰ����ʲô״̬����ѡֵ��STATUS_PULL_TO_REFRESH, STATUS_RELEASE_TO_REFRESH,
	 * STATUS_REFRESHING �� STATUS_REFRESH_FINISHED
	 */
	private int currentStatus = STATUS_REFRESH_FINISHED;;
	// ��¼��һ�ε�״̬��ʲô����������ظ�����
	private int lastStatus = currentStatus;
	// ����ˢ�µĻص��ӿ�
	private PullToRefreshListener mListener;
	// �ϴθ���ʱ��ĺ���ֵ
	private long lastUpdateTime;
	// һ���ӵĺ���ֵ�������ж��ϴεĸ���ʱ��
	public static final long ONE_MINUTE = 60 * 1000;
	// һСʱ�ĺ���ֵ�������ж��ϴεĸ���ʱ��
	public static final long ONE_HOUR = 60 * ONE_MINUTE;
	// һ��ĺ���ֵ�������ж��ϴεĸ���ʱ��
	public static final long ONE_DAY = 24 * ONE_HOUR;
	// һ�µĺ���ֵ�������ж��ϴεĸ���ʱ��
	public static final long ONE_MONTH = 30 * ONE_DAY;
	// һ��ĺ���ֵ�������ж��ϴεĸ���ʱ��
	public static final long ONE_YEAR = 12 * ONE_MONTH;
	// �ϴθ���ʱ����ַ���������������ΪSharedPreferences�ļ�ֵ
	private static final String UPDATED_AT = "updated_at";
	// Ϊ�˷�ֹ��ͬ���������ˢ�����ϴθ���ʱ���ϻ����г�ͻ��ʹ��id��������
	private int mId = -1;

	/**
	 * ����ˢ�¿ؼ��Ĺ��캯������������ʱ��̬���һ������ͷ�Ĳ��֡�
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
				// �����ָ���»�״̬����������ͷ����ȫ���صģ������������¼�
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
					// ͨ��ƫ������ͷ��topMarginֵ����ʵ������Ч��
					headerLayoutParams.topMargin = (distance / 2)
							+ hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
				}
				break;
			case MotionEvent.ACTION_UP:
			default:
				if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
					// ����ʱ������ͷ�����ˢ��״̬����ȥ��������ˢ�µ�����
					new RefreshingTask().execute();
				} else if (currentStatus == STATUS_PULL_TO_REFRESH) {
					// ����ʱ���������״̬����ȥ������������ͷ������
					new HideHeaderTask().execute();
				}
				break;
			}
			// ʱ�̼ǵø�������ͷ�е���Ϣ
			if (currentStatus == STATUS_PULL_TO_REFRESH
					|| currentStatus == STATUS_RELEASE_TO_REFRESH) {
				updateHeaderView();
				// ��ǰ�������������ͷ�״̬��Ҫ��ListViewʧȥ���㣬���򱻵������һ���һֱ����ѡ��״̬
				myListView.setPressed(false);
				myListView.setFocusable(false);
				myListView.setFocusableInTouchMode(false);
				lastStatus = currentStatus;
				// ��ǰ�������������ͷ�״̬��ͨ������true���ε�ListView�Ĺ����¼�
				return true;
			}
		}
		return false;
	}

	/**
	 * ���ݵ�ǰListView�Ĺ���״̬���趨 {@link #ableToPull}
	 * ��ֵ��ÿ�ζ���Ҫ��onTouch�е�һ��ִ�У����������жϳ���ǰӦ���ǹ���ListView������Ӧ�ý���������
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
				// ����׸�Ԫ�ص��ϱ�Ե�����븸����ֵΪ0����˵��ListView���������������ʱӦ����������ˢ��
				ableToPull = true;
			} else {
				if (headerLayoutParams.topMargin != hideHeaderHeight) {
					headerLayoutParams.topMargin = hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
				}
				ableToPull = false;
			}
		} else {
			// ���ListView��û��Ԫ�أ�ҲӦ����������ˢ��
			ableToPull = true;
		}
	}

	/**
	 * ��������ͷ�е���Ϣ��
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
	 * ���ݵ�ǰ��״̬����ת��ͷ��
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
	 * ˢ������ͷ���ϴθ���ʱ�������������
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
			String value = timeIntoFormat + "����";
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_DAY) {
			timeIntoFormat = timePassed / ONE_HOUR;
			String value = timeIntoFormat + "Сʱ";
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_MONTH) {
			timeIntoFormat = timePassed / ONE_DAY;
			String value = timeIntoFormat + "��";
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_YEAR) {
			timeIntoFormat = timePassed / ONE_MONTH;
			String value = timeIntoFormat + "����";
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		} else {
			timeIntoFormat = timePassed / ONE_YEAR;
			String value = timeIntoFormat + "��";
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		}
		updateAt.setText(updateAtValue);
	}

	/**
	 * ����ˢ�µ������ڴ������л�ȥ�ص�ע�����������ˢ�¼�������
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
	 * ��������ͷ�����񣬵�δ��������ˢ�»�����ˢ����ɺ󣬴����񽫻�ʹ����ͷ�������ء�
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
	 * ʹ��ǰ�߳�˯��ָ���ĺ�������
	 * 
	 * @param time
	 *            ָ����ǰ�߳�˯�߶�ã��Ժ���Ϊ��λ
	 */
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����ˢ�µļ�������ʹ������ˢ�µĵط�Ӧ��ע��˼���������ȡˢ�»ص���
	 * 
	 * @author guolin
	 */
	public interface PullToRefreshListener {
		/**
		 * ˢ��ʱ��ȥ�ص��˷������ڷ����ڱ�д�����ˢ���߼���ע��˷����������߳��е��õģ� ����Բ������߳������к�ʱ������
		 */
		void onRefresh();
	}
}
