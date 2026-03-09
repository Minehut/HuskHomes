/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.network;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class CustomBroker extends PluginMessageBroker {

    protected CustomBroker(@NotNull HuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws RuntimeException {
        // No setup
    }

    public final void onReceive(OnlineUser user, String subChannelId, @NotNull String encoded) {
        if (subChannelId != null && !subChannelId.equals(getSubChannelId())) {
            return;
        }

        final Message message = plugin.getMessageFromJson(encoded);

        if (message.getTargetType() == Message.TargetType.PLAYER) {
            super.plugin.getOnlineUsers().stream()
                    .filter(online -> message.getTarget().equals(Message.TARGET_ALL)
                            || online.getName().equals(message.getTarget()))
                    .forEach(receiver -> super.handle(receiver, message));
            return;
        }

        if (message.getTarget().equals(super.plugin.getServerName())
                || message.getTarget().equals(Message.TARGET_ALL)) {
            if (message.getType() == Message.MessageType.REQUEST_RTP_LOCATION) {
                super.handleRtpRequestLocation(message);
                return;
            }

            super.plugin.getOnlineUsers().stream()
                    .findAny()
                    .ifPresent(receiver -> super.handle(receiver, message));
        }
    }

    @Override
    protected void send(@NotNull Message message, @NotNull OnlineUser sender) {
        plugin.fireEvent(plugin.getBrokerMessageSendEvent(sender, getSubChannelId(), plugin.getGson().toJson(message)), null);
    }

    @Override
    public void changeServer(@NotNull OnlineUser user, @NotNull String server) {
        user.dismount().thenRun(() -> plugin.fireEvent(plugin.getBrokerChangeServerEvent(user, server), null));
    }
}
