package me.mumin.android.files.provider.sftp

import me.mumin.android.files.provider.common.PosixFileModeBit
import me.mumin.android.files.provider.common.toInt
import net.schmizz.sshj.sftp.FileAttributes

fun Set<PosixFileModeBit>.toSftpAttributes(): FileAttributes =
    FileAttributes.Builder().withPermissions(toInt()).build()
