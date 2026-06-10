/*
 * Insouciant Qualms © 2024 by Sascha Goldsmith is licensed under CC BY 4.0.
 * To view a copy of this license, visit https://creativecommons.org/licenses/by/4.0.
 * To reach the creator, visit https://www.linkedin.com/in/saschagoldsmith.
 */

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class GitWorktreesTest {

    private fun mainRepo(tmp: Path): Path {
        val main = Files.createDirectories(tmp.resolve("main"))
        Files.createDirectories(main.resolve(".git"))
        return main
    }

    private fun linkedWorktree(tmp: Path, main: Path, gitdirLine: String): Path {
        val worktree = Files.createDirectories(tmp.resolve("worktrees-home/session-1"))
        Files.createDirectories(main.resolve(".git/worktrees/session-1"))
        Files.writeString(worktree.resolve(".git"), gitdirLine)
        return worktree
    }

    @Test
    fun `main checkout with a git directory is not a linked worktree`(@TempDir tmp: Path) {
        val main = mainRepo(tmp)

        assertFalse(GitWorktrees.isLinkedWorktree(main))
        assertNull(GitWorktrees.mainRepositoryRoot(main))
    }

    @Test
    fun `directory without any git entry is not a linked worktree`(@TempDir tmp: Path) {
        assertFalse(GitWorktrees.isLinkedWorktree(tmp))
        assertNull(GitWorktrees.mainRepositoryRoot(tmp))
    }

    @Test
    fun `absolute gitdir pointer resolves to the main repository root`(@TempDir tmp: Path) {
        val main = mainRepo(tmp)
        val worktree = linkedWorktree(tmp, main, "gitdir: ${main.resolve(".git/worktrees/session-1")}\n")

        assertTrue(GitWorktrees.isLinkedWorktree(worktree))
        assertEquals(main, GitWorktrees.mainRepositoryRoot(worktree))
    }

    @Test
    fun `relative gitdir pointer resolves against the worktree root`(@TempDir tmp: Path) {
        val main = mainRepo(tmp)
        val worktree = linkedWorktree(tmp, main, "gitdir: ../../main/.git/worktrees/session-1\n")

        assertEquals(main, GitWorktrees.mainRepositoryRoot(worktree))
    }

    @Test
    fun `marker without a gitdir line yields null`(@TempDir tmp: Path) {
        val main = mainRepo(tmp)
        val worktree = linkedWorktree(tmp, main, "not a pointer\n")

        assertNull(GitWorktrees.mainRepositoryRoot(worktree))
    }

    @Test
    fun `gitdir pointer outside a worktrees directory yields null`(@TempDir tmp: Path) {
        val main = mainRepo(tmp)
        val worktree = linkedWorktree(tmp, main, "gitdir: ${main.resolve(".git")}\n")

        assertNull(GitWorktrees.mainRepositoryRoot(worktree))
    }

    @Test
    fun `blank gitdir pointer yields null`(@TempDir tmp: Path) {
        val main = mainRepo(tmp)
        val worktree = linkedWorktree(tmp, main, "gitdir:   \n")

        assertNull(GitWorktrees.mainRepositoryRoot(worktree))
    }
}
