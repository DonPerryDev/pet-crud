package com.donperry.model.pet

data class PhotoUploadData(
    val fileName: String,
    val contentType: String,
    val fileSize: Long,
    val fileBytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PhotoUploadData) return false
        return fileName == other.fileName &&
            contentType == other.contentType &&
            fileSize == other.fileSize &&
            fileBytes.contentEquals(other.fileBytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + fileBytes.contentHashCode()
        return result
    }
}
