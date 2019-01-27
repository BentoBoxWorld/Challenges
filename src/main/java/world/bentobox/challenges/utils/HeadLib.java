/*
 * Written in 2018 by Daniel Saukel
 *
 * To the extent possible under law, the author(s) have dedicated all
 * copyright and related and neighboring rights to this software
 * to the public domain worldwide.
 *
 * This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication
 * along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 *
 * @url https://github.com/DRE2N/HeadLib
 */
package world.bentobox.challenges.utils;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

import net.minecraft.server.v1_13_R2.NBTBase;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagList;


/**
 * @author Daniel Saukel
 *
 * BONNe modified it for BentoBox by leaving only mob heads and removing unused code.
 */
public enum HeadLib
{
// ---------------------------------------------------------------------
// Section: Library of All Mob heads
// ---------------------------------------------------------------------

	/**
	 * All enum values.
	 */
	SPIDER("8bdb71d0-4724-48b2-9344-e79480424798", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="),
	CAVE_SPIDER("39173a7a-c957-4ec1-ac1a-43e5a64983df", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDE2NDVkZmQ3N2QwOTkyMzEwN2IzNDk2ZTk0ZWViNWMzMDMyOWY5N2VmYzk2ZWQ3NmUyMjZlOTgyMjQifX19"),
	ENDERMAN("0de98464-1274-4dd6-bba8-370efa5d41a8", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E1OWJiMGE3YTMyOTY1YjNkOTBkOGVhZmE4OTlkMTgzNWY0MjQ1MDllYWRkNGU2YjcwOWFkYTUwYjljZiJ9fX0="),
	SLIME("7f0b0873-df6a-4a19-9bcd-f6c90ef804c7", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk1YWVlYzZiODQyYWRhODY2OWY4NDZkNjViYzQ5NzYyNTk3ODI0YWI5NDRmMjJmNDViZjNiYmI5NDFhYmU2YyJ9fX0="),
	GUARDIAN("f3898fe0-04fb-4f9c-8f8b-146a1d894007", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzI1YWY5NjZhMzI2ZjlkOTg0NjZhN2JmODU4MmNhNGRhNjQ1M2RlMjcxYjNiYzllNTlmNTdhOTliNjM1MTFjNiJ9fX0="),
	GHAST("807f287f-6499-4e93-a887-0a298ab3091f", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGI2YTcyMTM4ZDY5ZmJiZDJmZWEzZmEyNTFjYWJkODcxNTJlNGYxYzk3ZTVmOTg2YmY2ODU1NzFkYjNjYzAifX19"),
	BLAZE("7ceb88b2-7f5f-4399-abb9-7068251baa9d", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc4ZWYyZTRjZjJjNDFhMmQxNGJmZGU5Y2FmZjEwMjE5ZjViMWJmNWIzNWE0OWViNTFjNjQ2Nzg4MmNiNWYwIn19fQ=="),
	MAGMA_CUBE("96aced64-5b85-4b99-b825-53cd7a9f9726", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg5NTdkNTAyM2M5MzdjNGM0MWFhMjQxMmQ0MzQxMGJkYTIzY2Y3OWE5ZjZhYjM2Yjc2ZmVmMmQ3YzQyOSJ9fX0="),
	WITHER("119c371b-ea16-47c9-ad7f-23b3d894520a", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RmNzRlMzIzZWQ0MTQzNjk2NWY1YzU3ZGRmMjgxNWQ1MzMyZmU5OTllNjhmYmI5ZDZjZjVjOGJkNDEzOWYifX19"),
	ENDER_DRAGON("433562fa-9e23-443e-93b0-d67228435e77", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRlY2MwNDA3ODVlNTQ2NjNlODU1ZWYwNDg2ZGE3MjE1NGQ2OWJiNGI3NDI0YjczODFjY2Y5NWIwOTVhIn19fQ=="),
	SHULKER("d700b0b9-be74-4630-8cb5-62c979828ef6", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjFkMzUzNGQyMWZlODQ5OTI2MmRlODdhZmZiZWFjNGQyNWZmZGUzNWM4YmRjYTA2OWU2MWUxNzg3ZmYyZiJ9fX0="),
	CREEPER("eed2d903-ca32-4cc7-b33b-ca3bdbe18da4", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjQyNTQ4MzhjMzNlYTIyN2ZmY2EyMjNkZGRhYWJmZTBiMDIxNWY3MGRhNjQ5ZTk0NDQ3N2Y0NDM3MGNhNjk1MiJ9fX0="),
	ZOMBIE("9959dd98-efb3-4ee9-a8fb-2fda0218cda0", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTZmYzg1NGJiODRjZjRiNzY5NzI5Nzk3M2UwMmI3OWJjMTA2OTg0NjBiNTFhNjM5YzYwZTVlNDE3NzM0ZTExIn19fQ=="),
	ZOMBIE_VILLAGER("bcaf2b85-d421-47cc-a40a-455e77bfb60b", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdlODM4Y2NjMjY3NzZhMjE3YzY3ODM4NmY2YTY1NzkxZmU4Y2RhYjhjZTljYTRhYzZiMjgzOTdhNGQ4MWMyMiJ9fX0="),
	ZOMBIE_PIGMAN("6540c046-d6ea-4aff-9766-32a54ebe6958", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRlOWM2ZTk4NTgyZmZkOGZmOGZlYjMzMjJjZDE4NDljNDNmYjE2YjE1OGFiYjExY2E3YjQyZWRhNzc0M2ViIn19fQ=="),
	DOG("9655594c-5b1c-48a5-8e12-ffd7e0c735f2", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDk1MTgzY2E0Y2RkMjk2MjhmZTZjNzIyZjc3OTA4N2I4M2MyMWJhOTdmNDIyNWU0YWQ5YjNlNjE4ZWNjZDMwIn19fQ=="),
	HORSE("c6abc94e-a5ff-45fe-a0d7-4e479f290a6f", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDJlYjk2N2FiOTRmZGQ0MWE2MzI1ZjEyNzdkNmRjMDE5MjI2ZTVjZjM0OTc3ZWVlNjk1OTdmYWZjZjVlIn19fQ=="),
	TURTLE("ef56c7a3-a5e7-4a7f-9786-a4b6273a591d", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTJlNTQ4NDA4YWI3NWQ3ZGY4ZTZkNWQyNDQ2ZDkwYjZlYzYyYWE0ZjdmZWI3OTMwZDFlZTcxZWVmZGRmNjE4OSJ9fX0="),
	OCELOT("664dd492-3fcd-443b-9e61-4c7ebd9e4e10", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY1N2NkNWMyOTg5ZmY5NzU3MGZlYzRkZGNkYzY5MjZhNjhhMzM5MzI1MGMxYmUxZjBiMTE0YTFkYjEifX19"),
	SHEEP("fa234925-9dbe-4b8f-a544-7c70fb6b6ac5", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMxZjljY2M2YjNlMzJlY2YxM2I4YTExYWMyOWNkMzNkMThjOTVmYzczZGI4YTY2YzVkNjU3Y2NiOGJlNzAifX19"),
	COW("97ddf3b3-9dbe-4a3b-8a0f-1b19ddeac0bd", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWQ2YzZlZGE5NDJmN2Y1ZjcxYzMxNjFjNzMwNmY0YWVkMzA3ZDgyODk1ZjlkMmIwN2FiNDUyNTcxOGVkYzUifX19"),
	CHICKEN("7d3a8ace-e045-4eba-ab71-71dbf525daf1", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYzODQ2OWE1OTljZWVmNzIwNzUzNzYwMzI0OGE5YWIxMWZmNTkxZmQzNzhiZWE0NzM1YjM0NmE3ZmFlODkzIn19fQ=="),
	PIG("e1e1c2e4-1ed2-473d-bde2-3ec718535399", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIxNjY4ZWY3Y2I3OWRkOWMyMmNlM2QxZjNmNGNiNmUyNTU5ODkzYjZkZjRhNDY5NTE0ZTY2N2MxNmFhNCJ9fX0="),
	SQUID("f95d9504-ea2b-4b89-b2d0-d400654a7010", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMDE0MzNiZTI0MjM2NmFmMTI2ZGE0MzRiODczNWRmMWViNWIzY2IyY2VkZTM5MTQ1OTc0ZTljNDgzNjA3YmFjIn19fQ=="),
	MUSHROOM_COW("e206ac29-ae69-475b-909a-fb523d894336", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDBiYzYxYjk3NTdhN2I4M2UwM2NkMjUwN2EyMTU3OTEzYzJjZjAxNmU3YzA5NmE0ZDZjZjFmZTFiOGRiIn19fQ=="),
	ELDER_GUARDIAN("f2e933a7-614f-44e0-bf18-289b102104ab", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM3OTc0ODJhMTRiZmNiODc3MjU3Y2IyY2ZmMWI2ZTZhOGI4NDEzMzM2ZmZiNGMyOWE2MTM5Mjc4YjQzNmIifX19"),
	STRAY("644c9bad-958b-43ce-9d2f-199d85be607c", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzhkZGY3NmU1NTVkZDVjNGFhOGEwYTVmYzU4NDUyMGNkNjNkNDg5YzI1M2RlOTY5ZjdmMjJmODVhOWEyZDU2In19fQ=="),
	HUSK("2e387bc6-774b-4fda-ba22-eb54a26dfd9e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzc3MDY4MWQxYTI1NWZiNGY3NTQ3OTNhYTA1NWIyMjA0NDFjZGFiOWUxMTQxZGZhNTIzN2I0OTkzMWQ5YjkxYyJ9fX0="),
	SKELETON_HORSE("bcbce5bf-86c4-4e62-9fc5-0cc90de94b6d", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDdlZmZjZTM1MTMyYzg2ZmY3MmJjYWU3N2RmYmIxZDIyNTg3ZTk0ZGYzY2JjMjU3MGVkMTdjZjg5NzNhIn19fQ=="),
	ZOMBIE_HORSE("506ced1a-dac8-4d84-b341-645fbb297335", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q2YjllZjhkZDEwYmE2NDE0MjJiNDQ5ZWQxNWFkYzI5MmQ3M2Y1NzI5ODRkNDdlMjhhMjI2YWE2ZWRkODcifX19"),
	DONKEY("3da7917b-cb95-40b3-a516-9befa4f4d71d", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjEyNTJjMjI1MGM0NjhkOWZkZTUzODY3Nzg1NWJjOWYyODQzM2RmNjkyNDdkNzEzODY4NzgxYjgyZDE0YjU1In19fQ=="),
	MULE("fac6815e-02d5-4776-a5d6-f6d6535b7831", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzY5Y2E0YzI5NTZhNTY3Yzk2ZWUwNGM1MzE0OWYxODY0NjIxODM5M2JjN2IyMWVkNDVmZGFhMTNiZWJjZGFkIn19fQ=="),
	EVOKER("36ee7e5b-c092-48ad-9673-2a73b0a44b4f", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTAwZDNmZmYxNmMyZGNhNTliOWM1OGYwOTY1MjVjODY5NzExNjZkYmFlMTMzYjFiMDUwZTVlZTcxNjQ0MyJ9fX0="),
	VEX("f83bcfc1-0213-4957-888e-d3e2fae71203", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWU3MzMwYzdkNWNkOGEwYTU1YWI5ZTk1MzIxNTM1YWM3YWUzMGZlODM3YzM3ZWE5ZTUzYmVhN2JhMmRlODZiIn19fQ=="),
	VINDICATOR("5f958e1c-91ea-42d3-9d26-09e5925f2d9c", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2RhNTg1ZWJkZGNjNDhmMzA3YmU2YTgzOTE2Zjg3OGVkNGEwMTRlYzNkNGYyODZhMmNmZDk1MzI4MTk2OSJ9fX0="),
	ILLUSIONER("ccb79aa9-1764-4e5b-8ff3-e7be661ac7e2", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjYxNWUxMjQ1ZDBkODJkODFkZmEzNzUzMDYzZDhhYWQwZmE2NjU3NTk5ODcxY2Y0YzY5YmFiNzNjNjk5MDU1In19fQ=="),
	PIG_ZOMBIE("6540c046-d6ea-4aff-9766-32a54ebe6958", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRlOWM2ZTk4NTgyZmZkOGZmOGZlYjMzMjJjZDE4NDljNDNmYjE2YjE1OGFiYjExY2E3YjQyZWRhNzc0M2ViIn19fQ=="),
	SILVERFISH("30a4cd5c-5754-4db8-8960-18022a74627d", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGE5MWRhYjgzOTFhZjVmZGE1NGFjZDJjMGIxOGZiZDgxOWI4NjVlMWE4ZjFkNjIzODEzZmE3NjFlOTI0NTQwIn19fQ=="),
	BAT("cfdaf903-18cf-4a92-acf2-efa8626cf0b2", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWU5OWRlZWY5MTlkYjY2YWMyYmQyOGQ2MzAyNzU2Y2NkNTdjN2Y4YjEyYjlkY2E4ZjQxYzNlMGEwNGFjMWNjIn19fQ=="),
	WITCH("7f92b3d6-5ee0-4ab6-afae-2206b9514a63", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBlMTNkMTg0NzRmYzk0ZWQ1NWFlYjcwNjk1NjZlNDY4N2Q3NzNkYWMxNmY0YzNmODcyMmZjOTViZjlmMmRmYSJ9fX0="),
	ENDERMITE("33c425bb-a294-4e01-9b5b-a8ad652bb5cf", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODRhYWZmYTRjMDllMmVhZmI4NWQzNTIyMTIyZGIwYWE0NTg3NGJlYTRlM2Y1ZTc1NjZiNGQxNjZjN2RmOCJ9fX0="),
	WOLF("4aabc2be-340a-46ad-a42b-0c348344750a", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdhZGU0OWY1MDEzMTExNTExZGM1MWJhYjc2OWMxYWQ2OTUzMTlhNWQzNTViMzZhZTkyMzRlYTlkMWZmOGUifX19"),
	SNOWMAN("d71e165b-b49d-4180-9ccf-8ad3084df1dc", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTdlOTgzMWRhZjY4MWY4YzRjNDc3NWNiNDY1M2MzNGJlMjg5OGY4N2VmZDNiNTk4ZDU1NTUxOGYyZmFjNiJ9fX0="),
	IRON_GOLEM("7cb6e9a5-994f-40d5-9bfc-4ba5d796d21e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODkwOTFkNzllYTBmNTllZjdlZjk0ZDdiYmE2ZTVmMTdmMmY3ZDQ1NzJjNDRmOTBmNzZjNDgxOWE3MTQifX19"),
	RABBIT("2186bdc6-55b1-4b44-8a46-3c8a11d40f3d", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2QxMTY5YjI2OTRhNmFiYTgyNjM2MDk5MjM2NWJjZGE1YTEwYzg5YTNhYTJiNDhjNDM4NTMxZGQ4Njg1YzNhNyJ9fX0="),
	POLAR_BEAR("87324464-1700-468f-8333-e7779ec8c21e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDQ2ZDIzZjA0ODQ2MzY5ZmEyYTM3MDJjMTBmNzU5MTAxYWY3YmZlODQxOTk2NjQyOTUzM2NkODFhMTFkMmIifX19"),
	LLAMA("75fb08e5-2419-46fa-bf09-57362138f234", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzJiMWVjZmY3N2ZmZTNiNTAzYzMwYTU0OGViMjNhMWEwOGZhMjZmZDY3Y2RmZjM4OTg1NWQ3NDkyMTM2OCJ9fX0="),
	PARROT("da0cac14-3763-45df-b884-c99a567882ac", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTZkZTFlYjllMzI1ZTYyZjI4ZjJjMTgzZDM5YTY4MzExMzY0NDYzNjU3MjY0Njc1YThiNDYxY2QyOGM5In19fQ=="),
	VILLAGER("b3ed4a1b-dfff-464c-87c0-c8029e1de47b", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZhYjYxYWNlMTM2MDE3YTg3YjFiODFiMTQ1ZWJjNjNlMmU2ZGE5ZDM2NGM4MTE5NGIzM2VlODY2ZmU0ZCJ9fX0="),
	PHANTOM("9290add8-c291-4a5a-8f8a-594f165406a3", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2U5NTE1M2VjMjMyODRiMjgzZjAwZDE5ZDI5NzU2ZjI0NDMxM2EwNjFiNzBhYzAzYjk3ZDIzNmVlNTdiZDk4MiJ9fX0="),
	COD("d6d4c744-06b4-4a8a-bc7a-bdb0770bb1cf", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MmQ3ZGQ2YWFkZjM1Zjg2ZGEyN2ZiNjNkYTRlZGRhMjExZGY5NmQyODI5ZjY5MTQ2MmE0ZmIxY2FiMCJ9fX0="),
	SALMON("0354c430-3979-4b6e-8e65-a99eb3ea8818", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFlYjIxYTI1ZTQ2ODA2Y2U4NTM3ZmJkNjY2ODI4MWNmMTc2Y2VhZmU5NWFmOTBlOTRhNWZkODQ5MjQ4NzgifX19"),
	PUFFERFISH("258e3114-368c-48a1-85fd-be580912f0df", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTcxNTI4NzZiYzNhOTZkZDJhMjI5OTI0NWVkYjNiZWVmNjQ3YzhhNTZhYzg4NTNhNjg3YzNlN2I1ZDhiYiJ9fX0="),
	TROPICAL_FISH("d93c1bf6-616f-401a-af6e-f9b9803a0024", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc5ZTQ4ZDgxNGFhM2JjOTg0ZThhNmZkNGZiMTcwYmEwYmI0ODkzZjRiYmViZGU1ZmRmM2Y4Zjg3MWNiMjkyZiJ9fX0="),
	DROWNED("2f169660-61be-46bd-acb5-1abef9fe5731", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzNmN2NjZjYxZGJjM2Y5ZmU5YTYzMzNjZGUwYzBlMTQzOTllYjJlZWE3MWQzNGNmMjIzYjNhY2UyMjA1MSJ9fX0="),
	DOLPHIN("8b7ccd6d-36de-47e0-8d5a-6f6799c6feb8", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU5Njg4Yjk1MGQ4ODBiNTViN2FhMmNmY2Q3NmU1YTBmYTk0YWFjNmQxNmY3OGU4MzNmNzQ0M2VhMjlmZWQzIn19fQ==");

// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * User UUID that has given skin.
	 */
	private String uuid;

	/**
	 * Base64 Encoded link to Minecraft texture.
	 */
	private String textureValue;

	/**
	 * Skull owner.
	 */
	private Object skullOwner;

	/**
	 * This map allows to access all enum values via their string.
	 */
	private final static Map<String, HeadLib> BY_NAME = new HashMap<>();


// ---------------------------------------------------------------------
// Section: Constructor
// ---------------------------------------------------------------------


	/**
	 * This inits new enum value by given UUID and textureValue that is encoded in Base64.
	 * @param uuid User UUID String which skin must be loaded.
	 * @param textureValue Texture link encoded in Base64.
	 */
	HeadLib(String uuid, String textureValue)
	{
		this.uuid = uuid;
		this.textureValue = textureValue;
	}


// ---------------------------------------------------------------------
// Section: Methods that returns ItemStacks
// ---------------------------------------------------------------------

	/**
	 * Returns an ItemStack of the size 1 of the custom head.
	 *
	 * @return an ItemStack of the custom head.
	 */
	public ItemStack toItemStack()
	{
		return this.toItemStack(1);
	}


	/**
	 * Returns an ItemStack of the custom head.
	 *
	 * @param amount the amount of items in the stack
	 * @return an ItemStack of the custom head.
	 */
	public ItemStack toItemStack(int amount)
	{
		return this.toItemStack(amount, null);
	}


	/**
	 * Returns an ItemStack of the size 1 of the custom head.
	 *
	 * @param displayName the name to display. Supports "&amp;" color codes
	 * @param loreLines   optional lore lines. Supports "&amp;" color codes
	 * @return an ItemStack of the custom head.
	 */
	public ItemStack toItemStack(String displayName, String... loreLines)
	{
		return this.toItemStack(1, displayName, loreLines);
	}


	/**
	 * Returns an ItemStack of the custom head.
	 *
	 * @param amount      the amount of items in the stack
	 * @param displayName the name to display. Supports "&amp;" color codes
	 * @param loreLines   optional lore lines. Supports "&amp;" color codes
	 * @return an ItemStack of the custom head.
	 */
	public ItemStack toItemStack(int amount, String displayName, String... loreLines)
	{
		ItemStack item = new ItemStack(Material.PLAYER_HEAD, amount);

		if (displayName != null)
		{
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

			if (loreLines.length != 0)
			{
				List<String> loreCC = new ArrayList<>();
				Arrays.stream(loreLines).forEach(l -> loreCC.add(ChatColor.translateAlternateColorCodes('&', l)));
				meta.setLore(loreCC);
			}

			item.setItemMeta(meta);
		}

		return this.setSkullOwner(item, this.getSkullOwner());
	}


// ---------------------------------------------------------------------
// Section: Private inner methods
// ---------------------------------------------------------------------


	/**
	 * This method returns SkullOwner or create new one if it is not created yet.
	 * SkullOwner is NBTTagCompound object that contains information about player_head skin.
	 * @return skullOwner object.
	 */
	private Object getSkullOwner()
	{
		if (this.skullOwner == null)
		{
			this.skullOwner = this.createOwnerCompound(this.uuid, this.textureValue);
		}

		return this.skullOwner;
	}


	/**
	 * This method creates new NBTTagCompound object that contains UUID and texture link.
	 * @param id - UUID of user.
	 * @param textureValue - Encoded texture string.
	 * @return NBTTagCompound object that contains all necessary information about player_head skin.
	 */
	private NBTTagCompound createOwnerCompound(String id, String textureValue)
	{
		NBTTagCompound skullOwner = new NBTTagCompound();
		skullOwner.setString("Id", id);
		NBTTagCompound properties = new NBTTagCompound();
		NBTTagList textures = new NBTTagList();
		NBTTagCompound value = new NBTTagCompound();
		value.setString("Value", textureValue);
		textures.add(value);
		properties.set("textures", textures);
		skullOwner.set("Properties", properties);

		return skullOwner;
	}


	/**
	 * This method adds SkullOwner tag to given item.
	 * @param item Item whom SkullOwner tag must be added.
 	 * @param compound NBTTagCompound object that contains UUID and texture link.
	 * @return new copy of given item with SkullOwner tag.
	 */
	private ItemStack setSkullOwner(ItemStack item, Object compound)
	{
		net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		nmsStack.getOrCreateTag().set("SkullOwner", (NBTBase) compound);
		return CraftItemStack.asBukkitCopy(nmsStack);
	}


// ---------------------------------------------------------------------
// Section: Other methods
// ---------------------------------------------------------------------


	/**
	 * This method returns HeadLib enum object with given name. If enum value with given name does not exist,
	 * then return null.
	 * @param name Name of object that must be returned.
	 * @return HeadLib with given name or null.
	 */
	public static HeadLib getHead(String name)
	{
		return BY_NAME.get(name.toUpperCase());
	}


	//
	// This static call populates all existing enum values into static map.
	//
	static
	{
		for (HeadLib head : values())
		{
			BY_NAME.put(head.name(), head);
		}
	}
}