package de.christinecoenen.code.zapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.VideoView;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;
import de.christinecoenen.code.zapp.adapters.StreamPagePagerAdapter;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;
import de.christinecoenen.code.zapp.model.XmlResourcesChannelList;
import de.christinecoenen.code.zapp.utils.view.ClickableViewPager;
import de.christinecoenen.code.zapp.utils.view.FullscreenActivity;

public class ChannelDetailActivity extends FullscreenActivity {

	public static final String EXTRA_CHANNEL_ID = "de.christinecoenen.code.zapp.EXTRA_CHANNEL_ID";

	private static final String TAG = ChannelDetailActivity.class.getSimpleName();

	@BindView(R.id.viewpager_channels)
	ClickableViewPager viewPager;
	@BindView(R.id.video) VideoView videoView;
	@BindView(R.id.progressbar_video) ProgressBar progressView;

	@BindDrawable(android.R.drawable.ic_media_pause) Drawable pauseIcon;
	@BindDrawable(android.R.drawable.ic_media_play) Drawable playIcon;

	protected StreamPagePagerAdapter streamPagePagerAdapter;
	protected ChannelModel currentChannel;
	protected IChannelList channelList;
	protected boolean isPlaying = false;

	protected MediaPlayer.OnErrorListener videoErrorListener = new MediaPlayer.OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
			Log.d(TAG, "media player error: " + what + " - " + extra);
			return false;
		}
	};

	protected MediaPlayer.OnInfoListener videoInfoListener = new MediaPlayer.OnInfoListener() {
		@Override
		public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
			switch (what) {
				case MediaPlayer.MEDIA_INFO_BUFFERING_START:
					Log.d(TAG, "media player buffering start");
					progressView.setVisibility(View.VISIBLE);
					return true;
				case MediaPlayer.MEDIA_INFO_BUFFERING_END:
					Log.d(TAG, "media player buffering end");
					progressView.setVisibility(View.GONE);
					return true;
				case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
					Log.d(TAG, "media player rendering start");
					progressView.setVisibility(View.GONE);
					streamPagePagerAdapter.getCurrentFragment().onVideoStart();
					return true;
				default:
					return false;
			}
		}
	};

	protected StreamPagePagerAdapter.OnItemChangedListener onItemChangedListener =
			new StreamPagePagerAdapter.OnItemChangedListener() {
		@Override
		public void OnItemSelected(ChannelModel channel) {
			currentChannel = channel;
			setTitle(channel.getName());
			play();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ButterKnife.bind(this);

		channelList = new XmlResourcesChannelList(this);

		// set to channel
		Bundle extras = getIntent().getExtras();
		int channelId = extras.getInt(EXTRA_CHANNEL_ID);

		// listener
		videoView.setOnErrorListener(videoErrorListener);
		videoView.setOnInfoListener(videoInfoListener);

		// pager
		streamPagePagerAdapter = new StreamPagePagerAdapter(
				getSupportFragmentManager(), channelList, onItemChangedListener);
		viewPager.setAdapter(streamPagePagerAdapter);
		viewPager.setCurrentItem(channelId);
		viewPager.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mContentView.performClick();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		videoView.stopPlayback();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (isPlaying) {
			play();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_channel_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_settings:
				Intent videoIntent = new Intent(Intent.ACTION_VIEW);
				videoIntent.setDataAndType(Uri.parse(currentChannel.getStreamUrl()), "video/*");
				startActivity(videoIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected int getViewId() {
		return R.layout.activity_channel_detail;
	}

	@SuppressWarnings("unused")
	@OnTouch(R.id.viewpager_channels)
	public boolean onPagerTouch() {
		delayHide();
		return false;
	}

	protected void play() {
		Log.d(TAG, "play: " + currentChannel.getName());
		isPlaying = true;
		progressView.setVisibility(View.VISIBLE);
		videoView.setVideoPath(currentChannel.getStreamUrl());
		videoView.start();
	}
}