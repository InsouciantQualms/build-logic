/*
 * Insouciant Qualms © 2024 by Sascha Goldsmith is licensed under CC BY 4.0.
 * To view a copy of this license, visit https://creativecommons.org/licenses/by/4.0.
 * To reach the creator, visit https://www.linkedin.com/in/saschagoldsmith.
 */

import java.nio.file.Files
import java.nio.file.Path

/**
 * Detection helpers for git linked worktrees, usable from any convention plugin or build
 * script that must behave differently in a relocated worktree than in the main checkout —
 * most notably to resolve sibling builds (e.g. build-logic itself) against the main
 * repository rather than the worktree's own parent directory.
 *
 * Detection relies on git's on-disk worktree contract: in a linked worktree the checkout's
 * `.git` entry is a regular file (not a directory) containing a
 * `gitdir: <main>/.git/worktrees/<name>` pointer. The pointer may be relative; it is
 * resolved against the checkout root before climbing to the main repository.
 *
 * Bootstrap caveat: a consumer's settings script that uses this to *locate* build-logic
 * cannot call it (build-logic is not on the classpath until it has been located) and must
 * carry a minimal inline twin of [mainRepositoryRoot]; keep the two in sync.
 *
 * Thread-safety: stateless object; functions are pure over the filesystem state at call time.
 */
object GitWorktrees {

    /** True when the checkout at [checkoutRoot] is a linked git worktree rather than a main repository. */
    fun isLinkedWorktree(checkoutRoot: Path): Boolean = Files.isRegularFile(checkoutRoot.resolve(".git"))

    /** Main repository root for the linked worktree at [checkoutRoot], or null if not a worktree or the marker is malformed. */
    fun mainRepositoryRoot(checkoutRoot: Path): Path? {
        val marker = checkoutRoot.resolve(".git")
        if (!Files.isRegularFile(marker)) return null
        val gitdir = Files.readString(marker)
            .lineSequence()
            .firstOrNull { it.startsWith(GITDIR_PREFIX) }
            ?.removePrefix(GITDIR_PREFIX)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null
        val worktreeGitDir = checkoutRoot.resolve(gitdir).normalize()
        if (worktreeGitDir.parent?.fileName?.toString() != WORKTREES_DIR) return null
        return worktreeGitDir.parent?.parent?.parent
    }

    private const val GITDIR_PREFIX = "gitdir:"
    private const val WORKTREES_DIR = "worktrees"
}
