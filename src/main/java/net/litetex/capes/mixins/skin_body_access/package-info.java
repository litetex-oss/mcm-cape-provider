/**
 * This package (in combination with the corresponding package in access) is needed to access the body skin of a
 * player.
 * <p>
 * Context: Before 26.4 it was possible to access the NativeImage of a player's body via the TextureManager.
 * </p>
 * <p>
 * As of 26.4 this is required for SneakyCapes (to check if a player has a cape embedded in their texture or to load the
 * skin for the editor).
 * </p>
 *
 * @implNote Reasoning why this was solved like this:
 * <ol>
 * <li>There are multiple caches (by vanilla) when a skin is fetched, therefore this can't be directly returned as
 * it's impossible to determine where it's really needed</li>
 * <li>No NativeImage can be used directly because NativeImage requires manual cleanup (due to native memory)</li>
 * <li>Loading the skin again is also counterproductive as this work was already done</li>
 * </ol>
 */
package net.litetex.capes.mixins.skin_body_access;
