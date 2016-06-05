package com.jtransc.vfs

import com.jtransc.error.noImpl
import com.jtransc.io.stringz
import com.jtransc.numeric.nextMultipleOf
import com.jtransc.numeric.toInt
import com.jtransc.vfs.node.FileNodeIO
import com.jtransc.vfs.node.FileNodeTree
import com.jtransc.vfs.node.FileNodeType
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*

fun TarVfs(file: File): SyncVfsFile = TarSyncVfs(file.readBytes()).root()
fun TarVfs(file: ByteArray): SyncVfsFile = TarSyncVfs(file).root()

private class TarSyncVfs(val tarData: ByteArray) : BaseTreeVfs(FileNodeTree()) {
	var currentOffset = 0

	private fun readOne(): Boolean {
		val nodeHeaderOffset = currentOffset
		val header = ByteArrayInputStream(tarData, currentOffset, 0x200)
		val fileName = header.stringz(100)
		val fileMode = header.stringz(8)
		val ownerNumeric = header.stringz(8).toInt(8, 0)
		val ownerGroup = header.stringz(8).toInt(8, 0)
		val fileSize = header.stringz(12).toInt(8, 0)
		val lastModification = header.stringz(12).toInt(8, 0)
		val checksum = header.stringz(8)
		val linkIndicator = header.stringz(1)
		val nameOfLinkedFile = header.stringz(100)

		//0	100	File name
		//100	8	File mode
		//108	8	Owner's numeric user ID
		//116	8	Group's numeric user ID
		//124	12	File size in bytes (octal base)
		//136	12	Last modification time in numeric Unix time format (octal)
		//148	8	Checksum for header record
		//156	1	Link indicator (file type)
		//157	100	Name of linked file

		//0	156	(several fields, same as in old format)
		//156	1	Type flag
		//157	100	(same field as in old format)
		//257	6	UStar indicator "ustar" then NUL
		//263	2	UStar version "00"
		//265	32	Owner user name
		//297	32	Owner group name
		//329	8	Device major number
		//337	8	Device minor number
		//345	155	Filename prefix

		if (fileName.isNotEmpty()) {
			//println("$fileName : $fileMode : $fileSize")
			val node = tree.root[fileName, true]
			node.type = if (fileName.endsWith("/")) FileNodeType.DIRECTORY else FileNodeType.FILE
			node.io = object : FileNodeIO {
				override fun read(): ByteArray = Arrays.copyOfRange(tarData, nodeHeaderOffset + 0x200, nodeHeaderOffset + 0x200 + fileSize)
				override fun write(data: ByteArray) = noImpl("Writting not implemented on tar files")
				override fun size(): Long = fileSize.toLong()
				override fun mtime(): Date = Date(lastModification.toLong() * 1000L)
			}
		}
		currentOffset = (currentOffset + 0x200 + fileSize).nextMultipleOf(0x200)
		return fileName.isNotEmpty()
	}

	init {
		while (currentOffset < tarData.size) {
			if (!readOne()) break
		}
	}
}
