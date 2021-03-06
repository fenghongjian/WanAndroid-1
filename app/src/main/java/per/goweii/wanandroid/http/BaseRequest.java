package per.goweii.wanandroid.http;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import per.goweii.basic.core.receiver.LoginReceiver;
import per.goweii.rxhttp.core.RxLife;
import per.goweii.rxhttp.request.RxRequest;
import per.goweii.rxhttp.request.exception.ExceptionHandle;
import per.goweii.wanandroid.utils.UserUtils;

/**
 * @author CuiZhen
 * @date 2019/5/12
 * QQ: 302833254
 * E-mail: goweii@163.com
 * GitHub: https://github.com/goweii
 */
public class BaseRequest {

    protected static <T> Disposable request(@NonNull Observable<WanResponse<T>> observable, @NonNull RequestListener<T> listener) {
        return RxRequest.create(observable)
                .listener(new RxRequest.RequestListener() {
                    @Override
                    public void onStart() {
                        listener.onStart();
                    }

                    @Override
                    public void onError(ExceptionHandle handle) {
                        listener.onError(handle);
                        listener.onFailed(WanApi.ApiCode.ERROR, handle.getMsg());
                    }

                    @Override
                    public void onFinish() {
                        listener.onFinish();
                    }
                })
                .request(new RxRequest.ResultCallback<T>() {
                    @Override
                    public void onSuccess(int code, T data) {
                        listener.onSuccess(code, data);
                    }

                    @Override
                    public void onFailed(int code, String msg) {
                        if (code == WanApi.ApiCode.FAILED_NOT_LOGIN) {
                            UserUtils.getInstance().logout();
                            LoginReceiver.sendNotLogin();
                        }
                        listener.onFailed(code, msg);
                    }
                });
    }

    protected static <T> Disposable request(@NonNull Observable<WanResponse<T>> observable, @NonNull RequestListener<T> listener, ResponseToCache<T> responseToCache) {
        return RxRequest.create(observable)
                .listener(new RxRequest.RequestListener() {
                    @Override
                    public void onStart() {
                        listener.onStart();
                    }

                    @Override
                    public void onError(ExceptionHandle handle) {
                        listener.onError(handle);
                        listener.onFailed(WanApi.ApiCode.ERROR, handle.getMsg());
                    }

                    @Override
                    public void onFinish() {
                        listener.onFinish();
                    }
                })
                .request(new RxRequest.ResultCallback<T>() {
                    @Override
                    public void onSuccess(int code, T data) {
                        if (responseToCache.onResponce(data)) {
                            listener.onSuccess(code, data);
                        }
                    }

                    @Override
                    public void onFailed(int code, String msg) {
                        if (code == WanApi.ApiCode.FAILED_NOT_LOGIN) {
                            UserUtils.getInstance().logout();
                            LoginReceiver.sendNotLogin();
                        }
                        listener.onFailed(code, msg);
                    }
                });
    }

    protected static <T> void cacheAndNetList(RxLife rxLife,
                                              Observable<WanResponse<List<T>>> observable,
                                              String key,
                                              Class<T> clazz,
                                              RequestListener<List<T>> listener) {
        cacheAndNetList(rxLife, observable, false, key, clazz, listener);
    }

    protected static <T> void cacheAndNetList(RxLife rxLife,
                                              Observable<WanResponse<List<T>>> observable,
                                              boolean refresh,
                                              String key,
                                              Class<T> clazz,
                                              RequestListener<List<T>> listener) {
        if (refresh) {
            rxLife.add(request(observable, listener, new ResponseToCache<List<T>>() {
                @Override
                public boolean onResponce(List<T> resp) {
                    WanCache.getInstance().save(key, resp);
                    return true;
                }
            }));
            return;
        }
        rxLife.add(WanCache.getInstance().getList(key, clazz, new CacheListener<List<T>>() {
            @Override
            public void onSuccess(int code, final List<T> data) {
                listener.onSuccess(code, data);
                rxLife.add(request(observable, listener, new ResponseToCache<List<T>>() {
                    @Override
                    public boolean onResponce(List<T> resp) {
                        if (WanCache.getInstance().isSame(data, resp)) {
                            return false;
                        }
                        WanCache.getInstance().save(key, resp);
                        return true;
                    }
                }));
            }

            @Override
            public void onFailed() {
                rxLife.add(request(observable, listener, new ResponseToCache<List<T>>() {
                    @Override
                    public boolean onResponce(List<T> resp) {
                        WanCache.getInstance().save(key, resp);
                        return true;
                    }
                }));
            }
        }));
    }

    protected static <T> void cacheAndNetBean(RxLife rxLife,
                                              Observable<WanResponse<T>> observable,
                                              String key,
                                              Class<T> clazz,
                                              RequestListener<T> listener) {
        cacheAndNetBean(rxLife, observable, false, key, clazz, listener);
    }

    protected static <T> void cacheAndNetBean(RxLife rxLife,
                                              Observable<WanResponse<T>> observable,
                                              boolean refresh,
                                              String key,
                                              Class<T> clazz,
                                              RequestListener<T> listener) {
        if (refresh) {
            rxLife.add(request(observable, listener, new ResponseToCache<T>() {
                @Override
                public boolean onResponce(T resp) {
                    WanCache.getInstance().save(key, resp);
                    return true;
                }
            }));
            return;
        }
        rxLife.add(WanCache.getInstance().getBean(key, clazz, new CacheListener<T>() {
            @Override
            public void onSuccess(int code, T data) {
                listener.onSuccess(code, data);
                rxLife.add(request(observable, listener, new ResponseToCache<T>() {
                    @Override
                    public boolean onResponce(T resp) {
                        if (WanCache.getInstance().isSame(data, resp)) {
                            return false;
                        }
                        WanCache.getInstance().save(key, resp);
                        return true;
                    }
                }));
            }

            @Override
            public void onFailed() {
                rxLife.add(request(observable, listener, new ResponseToCache<T>() {
                    @Override
                    public boolean onResponce(T resp) {
                        WanCache.getInstance().save(key, resp);
                        return true;
                    }
                }));
            }
        }));
    }

    public interface ResponseToCache<T> {
        boolean onResponce(T resp);
    }

}
