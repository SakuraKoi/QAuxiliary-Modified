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

package cc.ioctl.hook;

import android.app.Activity;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cc.ioctl.fragment.ExfriendListFragment;
import io.github.qauxv.activity.SettingsUiFragmentHostActivity;
import io.github.qauxv.base.IUiItemAgent;
import io.github.qauxv.base.annotation.FunctionHookEntry;
import io.github.qauxv.base.annotation.UiItemAgentEntry;
import io.github.qauxv.dsl.FunctionEntryRouter.Locations.Auxiliary;
import io.github.qauxv.hook.BasePlainUiAgentItem;
import io.github.qauxv.hook.CommonSwitchFunctionHook;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;

@FunctionHookEntry
@UiItemAgentEntry
public class FriendDeletionNotification extends CommonSwitchFunctionHook {

    public static final FriendDeletionNotification INSTANCE = new FriendDeletionNotification();

    private FriendDeletionNotification() {
        super(true);
    }

    @Override
    protected boolean initOnce() throws Exception {
        return DeletionObserver.INSTANCE.initialize();
    }

    @NonNull
    @Override
    public String getName() {
        return "被删好友检测通知";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "检测到被删好友时将发出通知";
    }

    @NonNull
    @Override
    public String[] getUiItemLocation() {
        return Auxiliary.FRIEND_CATEGORY;
    }

    @NonNull
    @Override
    public List<Throwable> getRuntimeErrors() {
        return DeletionObserver.INSTANCE.getRuntimeErrors();
    }

    @UiItemAgentEntry
    public static class ExFriendListEntry extends BasePlainUiAgentItem {

        public static final ExFriendListEntry INSTANCE = new ExFriendListEntry();

        private ExFriendListEntry() {
            super("历史好友", "得不到的永远骚动, 被偏爱的都有恃无恐.");
        }

        @Nullable
        @Override
        public Function3<IUiItemAgent, Activity, View, Unit> getOnClickListener() {
            return (agent, activity, view) -> {
                SettingsUiFragmentHostActivity.startFragmentWithContext(activity,
                        ExfriendListFragment.class, null);
                return Unit.INSTANCE;
            };
        }

        @NonNull
        @Override
        public String[] getUiItemLocation() {
            return Auxiliary.FRIEND_CATEGORY;
        }
    }
}
