package com.example.transport

import com.example.model.AlertPriority
import com.example.transport.NetworkPacket
import com.example.transport.proto.NetworkPacketProto

object ProtobufSerializer {

    fun encode(packet: NetworkPacket): ByteArray {
        val proto = NetworkPacketProto.newBuilder()
            .setPacketId(packet.packetId)
            .setSenderId(packet.senderId)
            .setSenderCallsign(packet.senderCallsign)
            .setText(packet.text)
            .setLanguageCode(packet.languageCode)
            .setIsAlert(packet.isAlert)
            .setAlertPriority(packet.alertPriority.name)
            .setTimestamp(packet.timestamp)
            .setChannelFreq(packet.channelFreq)
            .setTtl(packet.ttl)
            .setRelayHops(packet.relayHops)
            .build()

        return proto.toByteArray()
    }

    fun decode(bytes: ByteArray): NetworkPacket {
        val proto = NetworkPacketProto.parseFrom(bytes)
        val alertPriority = try {
            AlertPriority.valueOf(proto.alertPriority)
        } catch (e: Exception) {
            AlertPriority.ROUTINE
        }
        
        return NetworkPacket(
            packetId = proto.packetId,
            senderId = proto.senderId,
            senderCallsign = proto.senderCallsign,
            text = proto.text,
            languageCode = proto.languageCode,
            isAlert = proto.isAlert,
            alertPriority = alertPriority,
            timestamp = proto.timestamp,
            channelFreq = proto.channelFreq,
            ttl = proto.ttl,
            relayHops = proto.relayHops
        )
    }
}
