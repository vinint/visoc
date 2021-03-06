package com.dovar.router_api.multiprocess;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.dovar.router_api.ILocalRouterAIDL;
import com.dovar.router_api.router.Router;
import com.dovar.router_api.router.RouterUtil;


/**
 * auther by heweizong on 2018/8/21
 * description: 用于多进程时，本地进程与广域路由通信
 * {@link MultiRouter#connectLocalRouter(String)}
 * 在MultiRouter中绑定启动，MultiRouter持有代表各个进程的ConnectMultiRouterService的ILocalRouterAIDL
 */
public class ConnectMultiRouterService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mLocalRouterAIDL;
    }

    private final ILocalRouterAIDL.Stub mLocalRouterAIDL = new ILocalRouterAIDL.Stub() {

        @Override
        public MultiRouterResponse route(MultiRouterRequest routerRequest) throws RemoteException {
            try {
                return RouterUtil.createMultiResponse(Router.instance().localRoute(RouterUtil.backToRequest(routerRequest)));
            } catch (Exception e) {
                e.printStackTrace();
                MultiRouterResponse multiResponse = new MultiRouterResponse();
                multiResponse.setMessage(ConnectMultiRouterService.this.getClass().getSimpleName() + ":" + e.getMessage());
                return multiResponse;
            }
        }

        @Override
        public boolean stopWideRouter() throws RemoteException {
            Router.instance().unbindMultiRouter();
            return true;
        }

        @Override
        public void connectWideRouter() throws RemoteException {
            Router.instance().bindMultiRouter();
        }
    };
}
