/*
 * QAuxiliary - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2022 qwq233@qwq2333.top
 * https://github.com/cinit/QAuxiliary
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by QAuxiliary contributors.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/cinit/QAuxiliary/blob/master/LICENSE.md>.
 */
package io.github.qauxv.bridge;

import static io.github.qauxv.bridge.AppRuntimeHelper.getQQAppInterface;

import androidx.annotation.NonNull;
import cc.ioctl.util.HostInfo;
import cc.ioctl.util.Reflex;
import io.github.qauxv.tlb.ConfigTable;
import io.github.qauxv.util.DexKit;
import io.github.qauxv.util.Initiator;
import io.github.qauxv.util.Log;
import io.github.qauxv.util.QQVersion;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

public class QQMessageFacade {

    private QQMessageFacade() {
    }

    public static Object get() {
        try {
            return Reflex.invokeVirtualAny(AppRuntimeHelper.getQQAppInterface(), Initiator._QQMessageFacade());
        } catch (Exception e) {
            Log.e("QQMessageFacade.get() failed!");
            Log.e(e);
            return null;
        }
    }

    public static Object getMessageManager(int istroop) {
        try {
            if (HostInfo.requireMinQQVersion(QQVersion.QQ_8_6_0)) {
                return Reflex.invokeVirtualDeclaredFixedModifierOrdinal(get(), Modifier.PUBLIC, 0,
                    Initiator._BaseQQMessageFacade(), 0, 1, true, istroop,
                    int.class, Initiator._BaseMessageManager());
            }
            return Reflex.invokeVirtualDeclaredModifierAny(get(), Modifier.PUBLIC, 0, istroop,
                int.class, Initiator._BaseMessageManager());
        } catch (Exception e) {
            Log.e("QQMessageFacade.getMessageManager() failed!");
            Log.e(e);
            return null;
        }
    }

    public static void revokeMessage(Object msg) throws Exception {
        if (msg == null) {
            throw new NullPointerException("msg == null");
        }
        int istroop = (int) Reflex.getInstanceObjectOrNull(msg, "istroop");
        Object mgr = getMessageManager(istroop);
        try {
            Object msg2 = Reflex.invokeStaticAny(DexKit.doFindClass(DexKit.C_MSG_REC_FAC), msg,
                Initiator._MessageRecord(), Initiator._MessageRecord());
            long t = (long) Reflex.getInstanceObjectOrNull(msg2, "time");
            t -= 1 + 10f * Math.random();
            Reflex.setInstanceObject(msg2, "time", t);
            Object msgCache = Reflex.invokeVirtualAny(getQQAppInterface(),
                    DexKit.doFindClass(DexKit.C_MessageCache));
            String methodName = HostInfo.requireMinQQVersion(QQVersion.QQ_8_8_93) ? "z2" : "b"; //Default method name for QQ
            if (HostInfo.isTim()) {
                methodName = ConfigTable.INSTANCE.getConfig(QQMessageFacade.class.getSimpleName());
            }
            Reflex.invokeVirtual(msgCache, methodName, true, boolean.class, void.class);
            if (HostInfo.requireMinQQVersion(QQVersion.QQ_8_6_0)) {
                Reflex.invokeVirtualDeclaredFixedModifierOrdinal(mgr, Modifier.PUBLIC, 0,
                    Initiator._BaseMessageManager(), 4, 7, true, msg2, Initiator._MessageRecord(),
                    void.class);
            } else {
                Reflex.invokeVirtualDeclaredFixedModifierOrdinal(mgr, Modifier.PUBLIC, 0,
                    Initiator._BaseMessageManager(), 2, 4, true, msg2, Initiator._MessageRecord(),
                    void.class);
            }
        } catch (Exception e) {
            Log.e("revokeMessage failed: " + msg);
            Log.e(e);
            throw e;
        }
    }

    public static void commitMessageRecordList(@NonNull List<Object> messages) throws ReflectiveOperationException {
        commitMessageRecordList(messages, Objects.requireNonNull(AppRuntimeHelper.getAccount(), "account == null"));
    }

    public static void commitMessageRecordList(@NonNull List<Object> messages, @NonNull String account) throws ReflectiveOperationException {
        Objects.requireNonNull(messages, "messages == null");
        Objects.requireNonNull(account, "account == null");
        if (messages.isEmpty()) {
            return;
        }
        if (HostInfo.requireMinQQVersion(QQVersion.QQ_8_8_93)) {
            Reflex.invokeVirtual(ManagerHelper.getQQMessageFacade(), "h", messages, account,
                    List.class, String.class, void.class);
        } else if (HostInfo.requireMinQQVersion(QQVersion.QQ_8_6_0)) {
            Reflex.invokeVirtual(ManagerHelper.getQQMessageFacade(), "a", messages, account,
                    List.class, String.class, void.class);
        } else {
            Reflex.invokeVirtualDeclaredOrdinalModifier(
                    ManagerHelper.getQQMessageFacade(), 0, 4, false, Modifier.PUBLIC, 0,
                    messages, account,
                    List.class, String.class, void.class);
        }
    }
}
