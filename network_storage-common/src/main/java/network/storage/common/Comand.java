package network.storage.common;

public class  Comand {
    public static final byte WRITE_FILE = (byte)1;
    public static final byte DELETE_FILE_FromServer = (byte)2;
    public static final byte RENAME_FILE_FromServer = (byte)3;
    public static final byte DOWNLOAD_FILE_TO_CLIENT = (byte)4;
    public static final byte DELETE_FILE_FROM_CLIENT = (byte)5;
    public static final byte RENAME_FILE_TO_CLIENT = (byte)6;
    public static final byte TRY_TO_AUTH = (byte)10;
    public static final byte TRY_TO_SIGNUP = (byte)11;
    public static final byte AUTH_NOT_OK = (byte)12;
    public static final byte AUTH_OK = (byte)13;
    public static final byte CLIENT_CLOSE = (byte)16;
    public static final byte INFO = (byte)17;
}
