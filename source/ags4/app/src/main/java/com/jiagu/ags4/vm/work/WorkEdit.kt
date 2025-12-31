package com.jiagu.ags4.vm.work

import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.repo.Repo
import com.jiagu.ags4.repo.net.AgsNet


interface IWorkEdit {
    suspend fun buildAndSaveBlock(
        name: String,
        buildBlock: () -> Block,
        complete: (List<Long>) -> Unit,
    )

    suspend fun saveBlock(block: Block, complete: (List<Long>) -> Unit)
    suspend fun updateBlock(block: Block, complete: (List<Long>) -> Unit)
    suspend fun deleteBlock(localBlockId: Long, complete: () -> Unit)
    suspend fun renameBlock(localBlockId: Long, name: String, complete: () -> Unit)
}

class WorkEditImpl : IWorkEdit {
    override suspend fun buildAndSaveBlock(
        name: String,
        buildBlock: () -> Block,
        complete: (List<Long>) -> Unit,
    ) {
        val block = buildBlock()
        saveBlock(block, complete = complete)
    }

    override suspend fun saveBlock(block: Block, complete: (List<Long>) -> Unit) {
        Repo.uploadBlocks(listOf(block)).collect {
            complete(it)
        }
    }

    override suspend fun updateBlock(block: Block, complete: (List<Long>) -> Unit) {
        if (block.localBlockId == 0L) {
            AgsNet.updateBlocksSync(block)
            complete(listOf(block.localBlockId))
        } else {
            Repo.updateBlockAndClearPlan(block).collect {
                complete(listOf(block.localBlockId))
            }
        }
    }

    override suspend fun deleteBlock(localBlockId: Long, complete: () -> Unit) {
        Repo.deleteBlock(localBlockId).collect {
            complete()
        }
    }

    override suspend fun renameBlock(localBlockId: Long, name: String, complete: () -> Unit) {
        Repo.getBlockDetail(localBlockId).collect { block ->
            block.blockName = name
            Repo.updateBlockName(block).collect {
                complete()
            }
        }
    }
}
