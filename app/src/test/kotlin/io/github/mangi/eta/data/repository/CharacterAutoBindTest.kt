package io.github.mangi.eta.data.repository

import io.github.mangi.eta.data.db.CharacterEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** 新建会话自动绑定角色卡的选取规则。 */
class CharacterAutoBindTest {
    private fun card(id: String, archived: Boolean = false) = CharacterEntity(
        id = id,
        name = id,
        cardJson = "{}",
        avatarPath = null,
        archived = archived,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun singleCardIsAlwaysBound() {
        assertEquals("only", CharacterRepository.autoBindCandidate(listOf(card("only"))))
    }

    @Test
    fun multipleCardsUseTheFirstOneInLibraryOrder() {
        // DAO 按 updated_at DESC 返回，列表首位即角色库界面的第一张。
        val rows = listOf(card("first"), card("second"), card("third"))
        assertEquals("first", CharacterRepository.autoBindCandidate(rows))
    }

    @Test
    fun archivedCardsAreSkipped() {
        val rows = listOf(card("archived", archived = true), card("active"))
        assertEquals("active", CharacterRepository.autoBindCandidate(rows))
    }

    @Test
    fun emptyLibraryBindsNothing() {
        assertNull(CharacterRepository.autoBindCandidate(emptyList()))
        assertNull(CharacterRepository.autoBindCandidate(listOf(card("gone", archived = true))))
    }
}
