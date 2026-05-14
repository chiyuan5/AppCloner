package android.app.ifma.mts.binderceptor;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IBinderceptorCallback extends IInterface {

    void onBinderTransaction(String service, int code, byte[] data, byte[] reply);

    void onBinderOneway(String service, int code, byte[] data);

    abstract class Stub extends Binder implements IBinderceptorCallback {
        public Stub() {
            attachInterface(this, "android.app.ifma.mts.binderceptor.IBinderceptorCallback");
        }

        public static IBinderceptorCallback asInterface(IBinder binder) {
            return null;
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        protected boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) {
            return false;
        }
    }
}
