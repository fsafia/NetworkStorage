package network.storage.common;

public enum  Comand {
    WRITE_FILE((byte)1),
    DELETE_FILE_FromServer((byte)2),
    RENAME_FILE_FromServer((byte)3),
    DOWNLOAD_FILE_ToClient((byte)4),
    DELETE_FILE_FromClient((byte)5),
    RENAME_FILE_ToClient((byte)6),
    TRY_TO_AUTH((byte)10),
    TRY_TO_SIGNUP((byte)11),
    AUTH_NOT_OK((byte)12),
    AUTH_OK((byte)13);

    private byte numberComand;
    public byte getNumberComand() {
        return numberComand;
    }

    private Comand(byte numberComand){
        this.numberComand = numberComand;
    }
}
