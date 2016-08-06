package com.hiroshi.cimoc.presenter;

import android.util.Log;

import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.ui.activity.StreamReaderActivity;
import com.hiroshi.cimoc.ui.adapter.PreloadAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class StreamReaderPresenter extends BasePresenter {

    private final static int LOAD_NULL = 0;
    private final static int LOAD_PREV = 1;
    private final static int LOAD_NEXT = 2;

    private StreamReaderActivity mStreamReaderActivity;
    private PreloadAdapter mPreloadAdapter;
    private Manga mManga;

    private String cid;
    private String last;
    private Integer page;
    private int status;

    public StreamReaderPresenter(StreamReaderActivity activity, int source, String cid, String last, Integer page, Chapter[] array, int position) {
        mStreamReaderActivity = activity;
        mPreloadAdapter = new PreloadAdapter(array, position);
        mManga = Kami.getMangaById(source);
        this.cid = cid;
        this.last = last;
        this.page = page;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        status = LOAD_NEXT;
        mManga.browse(cid, mPreloadAdapter.getNextChapter().getPath());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mManga.cancel();
    }

    public void loadNext() {
        if (status == LOAD_NULL) {
            Chapter chapter = mPreloadAdapter.getNextChapter();
            if (chapter != null) {
                status = LOAD_NEXT;
                mManga.browse(cid, chapter.getPath());
            }
        }
    }

    public void onScrolled(int dy, int last, int count) {
        if (last >= count - 1 && dy > 0 && status == LOAD_NULL) {
            Chapter chapter = mPreloadAdapter.getNextChapter();
            if (chapter != null) {
                status = LOAD_NEXT;
                mManga.browse(cid, chapter.getPath());
            }
        }
        /*else if (last <= 1 && dy < 0 && status == LOAD_NULL) {
            Chapter chapter = mPreloadAdapter.getPrevChapter();
            if (chapter != null) {
                status = LOAD_PREV;
                mManga.browse(cid, chapter.getPath());
            }
        }*/
    }

    public void setPage(int progress) {
        EventBus.getDefault().post(new EventMessage(EventMessage.COMIC_PAGE_CHANGE, progress));
    }

    public void onChapterToNext() {
        Chapter chapter = mPreloadAdapter.nextChapter();
        if (chapter != null) {
            switchChapter(1, chapter.getCount(), chapter.getTitle(), chapter.getPath());
        }
    }

    public void onChapterToPrev() {
        Chapter chapter = mPreloadAdapter.prevChapter();
        if (chapter != null) {
            switchChapter(chapter.getCount(), chapter.getCount(), chapter.getTitle(), chapter.getPath());
        }
    }

    public void onProgressChanged(int value, boolean fromUser) {
    }

    private void switchChapter(int progress, int max, String title, String path) {
        mStreamReaderActivity.updateChapterInfo(max, title);
        if (progress != -1) {
            mStreamReaderActivity.setReadProgress(progress);
        }
        EventBus.getDefault().post(new EventMessage(EventMessage.COMIC_LAST_CHANGE, path));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.PARSE_PIC_SUCCESS:
                String[] array = (String[]) msg.getData();
                Chapter chapter;
                if (status == LOAD_PREV) {
                    mStreamReaderActivity.setPrevImage(array);
                    chapter = mPreloadAdapter.movePrev();
                } else {
                    mStreamReaderActivity.setNextImage(array);
                    chapter = mPreloadAdapter.moveNext();
                }
                chapter.setCount(array.length);
                switchChapter(1, array.length, chapter.getTitle(), chapter.getPath());
                status = LOAD_NULL;
                break;
            case EventMessage.PARSE_PIC_FAIL:
            case EventMessage.NETWORK_ERROR:
                break;
        }
    }

}
