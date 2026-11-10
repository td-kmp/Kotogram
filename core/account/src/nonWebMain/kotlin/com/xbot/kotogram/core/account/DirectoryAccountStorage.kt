package com.xbot.kotogram.core.account

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

internal class DirectoryAccountStorage(
    private val root: String,
) : AccountStorage {
    override fun databaseDirectory(id: AccountId): String = "${directoryOf(id)}/db"

    override fun filesDirectory(id: AccountId): String = "${directoryOf(id)}/files"

    override suspend fun delete(id: AccountId) = deleteRecursively(Path(directoryOf(id)))

    private fun directoryOf(id: AccountId) = "$root/${id.value}"

    private fun deleteRecursively(path: Path) {
        if (!SystemFileSystem.exists(path)) return
        if (SystemFileSystem.metadataOrNull(path)?.isDirectory == true) {
            SystemFileSystem.list(path).forEach(::deleteRecursively)
        }
        SystemFileSystem.delete(path, mustExist = false)
    }
}
