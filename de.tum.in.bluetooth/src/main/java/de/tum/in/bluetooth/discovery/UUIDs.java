package de.tum.in.bluetooth.discovery;

import javax.bluetooth.UUID;

public interface UUIDs {

	public static final UUID SDP = new UUID(0x0001);

	public static final UUID RFCOMM = new UUID(0x0003);

	public static final UUID ATT = new UUID(0x0007);

	public static final UUID OBEX = new UUID(0x0008);

	public static final UUID HTTP = new UUID(0x000C);

	public static final UUID L2CAP = new UUID(0x0100);

	public static final UUID BNEP = new UUID(0x000F);

	public static final UUID SERIAL_PORT = new UUID(0x1101);

	public static final UUID SERVICE_DISCOVERY_SERVER_SERVICE_CLASSID = new UUID(
			0x1000);

	public static final UUID BROWSE_GROUP_DESCRIPTOR_SERVICE_CLASSID = new UUID(
			0x1001);

	public static final UUID PUBLIC_BROWSE_GROUP = new UUID(0x1002);

	public static final UUID OBEX_OBJECT_PUSH_PROFILE = new UUID(0x1105);

	public static final UUID OBEX_FILE_TRANSFER_PROFILE = new UUID(0x1106);

	public static final UUID PERSONAL_AREA_NETWORKING_USER = new UUID(0x1115);

	public static final UUID NETWORK_ACCESS_POUUID = new UUID(0x1116);

	public static final UUID GROUP_NETWORK = new UUID(0x1117);
}
