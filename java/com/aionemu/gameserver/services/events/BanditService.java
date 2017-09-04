/*
 * This file is part of the Aion-Emu project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.services.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TARGET_SELECTED;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TRANSFORM;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.DispelCategoryType;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.skillengine.model.TransformType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * Created by wanke on 13/02/2017.
 */
public class BanditService
{
	private static Logger log = LoggerFactory.getLogger(BanditService.class);
	
	public void onInit()
	{
		log.info("[PK] Bandit started!");
	}
	
	public void startBandit(Player player)
	{
		player.getEffectController().setAbnormal(AbnormalState.SLEEP.getId());
		player.getEffectController().updatePlayerEffectIcons();
		player.getEffectController().broadCastEffects();
		final ActionObserver observer = new ActionObserver(ObserverType.ATTACKED)
		{
			@Override
			public void attacked(Creature creature)
			{
				if (player.getController().hasTask(TaskId.PK))
				{
					player.getController().cancelTask(TaskId.PK);
					player.getEffectController().unsetAbnormal(AbnormalState.SLEEP.getId());
					player.getEffectController().updatePlayerEffectIcons();
					player.getEffectController().broadCastEffects();
				}
			}
		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.PK, ThreadPoolManager.getInstance().schedule(() ->
		{
			player.getObserveController().removeObserver(observer);
			if (player.getLifeStats().isAlreadyDead())
			{
				PlayerReviveService.skillRevive(player);
			}
			if (player.isInGroup2())
			{
				PlayerGroupService.removePlayer(player);
			}
			if (player.isInAlliance2())
			{
				PlayerAllianceService.removePlayer(player);
			}
			player.getEffectController().unsetAbnormal(AbnormalState.SLEEP.getId());
			player.getEffectController().updatePlayerEffectIcons();
			player.getEffectController().broadCastEffects();
			player.setBandit(true);
			player.setAdminEnmity(2);
			player.setAdminNeutral(0);
			morphBandit(player, false);
			player.clearKnownlist();
			sendAnnounce(player);
			PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player, false));
			PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
			player.getEffectController().updatePlayerEffectIcons();
			player.updateKnownlist();
			TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(), player.getX(), player.getY(), player.getZ(), player.getHeading());
		}, 5 * 1000));
	}
	
	public void stopBandit(Player player)
	{
		player.getEffectController().setAbnormal(AbnormalState.SLEEP.getId());
		player.getEffectController().updatePlayerEffectIcons();
		player.getEffectController().broadCastEffects();
		final ActionObserver observer = new ActionObserver(ObserverType.ATTACKED)
		{
			@Override
			public void attacked(Creature creature)
			{
				if (player.getController().hasTask(TaskId.PK))
				{
					player.getController().cancelTask(TaskId.PK);
					player.getEffectController().unsetAbnormal(AbnormalState.SLEEP.getId());
					player.getEffectController().updatePlayerEffectIcons();
					player.getEffectController().broadCastEffects();
				}
			}
		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.PK, ThreadPoolManager.getInstance().schedule(() ->
		{
			player.getObserveController().removeObserver(observer);
			if (player.getLifeStats().isAlreadyDead())
			{
				PlayerReviveService.skillRevive(player);
			}
			if (player.isInGroup2())
			{
				PlayerGroupService.removePlayer(player);
			}
			if (player.isInAlliance2())
			{
				PlayerAllianceService.removePlayer(player);
			}
			player.getEffectController().unsetAbnormal(AbnormalState.SLEEP.getId());
			player.getEffectController().updatePlayerEffectIcons();
			player.getEffectController().broadCastEffects();
			player.setBandit(false);
			player.setAdminEnmity(0);
			player.setAdminNeutral(0);
			morphBandit(player, true);
			player.clearKnownlist();
			PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player, false));
			PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
			player.getEffectController().updatePlayerEffectIcons();
			player.updateKnownlist();
			TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(), player.getX(), player.getY(), player.getZ(), player.getHeading());
		}, 5 * 1000));
	}
	
	public void onDie(Player player, Creature lastAttacker)
	{
		final Summon summon = player.getSummon();
		if (summon != null)
		{
			summon.getController().release(UnsummonType.UNSPECIFIED);
		}
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.DIE, 0, lastAttacker == null ? 0 : lastAttacker.getObjectId()), true);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DEATH_MESSAGE_ME);
		player.getMoveController().abortMove();
		player.setState(CreatureState.DEAD);
		player.getObserveController().notifyDeathObservers(player);
		player.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.DEBUFF);
		player.getEffectController().removeEffectByDispelCat(DispelCategoryType.ALL, SkillTargetSlot.DEBUFF, 100, 2, 100, false);
		player.setTarget(null);
		PacketSendUtility.sendPacket(player, new SM_TARGET_SELECTED(player));
		ThreadPoolManager.getInstance().schedule(() ->
		{
			if (player.isBandit())
			{
				if (player.getLifeStats().isAlreadyDead())
				{
					PlayerReviveService.banditRevive(player);
				}
				player.setBandit(false);
				player.setAdminEnmity(0);
				player.setAdminNeutral(0);
				player.clearKnownlist();
				morphBandit(player, true);
				sendDieAnnounce(player, (Player) lastAttacker);
				PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player, false));
				PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
				player.getEffectController().updatePlayerEffectIcons();
				player.updateKnownlist();
				TeleportService2.moveToBindLocation(player, true);
			}
		}, 6000);
	}
	
	public void morphBandit(Player player, boolean die)
	{
		if (!die)
		{
			player.getTransformModel().setModelId(219655); // Bloodthirsty Vampidaru.
			player.getTransformModel().setPanelId(0);
			player.getTransformModel().setTransformType(TransformType.PC);
			PacketSendUtility.broadcastPacketAndReceive(player, new SM_TRANSFORM(player, 0, true, 0));
			player.setTransformed(true);
			player.setTransformedModelId(219655); // Bloodthirsty Vampidaru.
		}
		else
		{
			player.getTransformModel().setModelId(0);
			player.getTransformModel().setTransformType(TransformType.PC);
			player.getTransformModel().setPanelId(0);
			PacketSendUtility.broadcastPacketAndReceive(player, new SM_TRANSFORM(player, 0, false, 0));
			player.setTransformed(false);
			player.setTransformedModelId(0);
		}
	}
	
	public void sendAnnounce(Player player)
	{
		World.getInstance().doOnAllPlayers(pl ->
		{
			if ((pl.getWorldId() == player.getWorldId()) && (pl != player))
			{
				PacketSendUtility.sendSys3Message(pl, "[PK] Bandit", "A player just passed <Outlaw>, RUN!");
			}
		});
	}
	
	public void sendDieAnnounce(Player looser, Player killer)
	{
		World.getInstance().doOnAllPlayers(pl ->
		{
			if (pl.getWorldId() == looser.getWorldId())
			{
				PacketSendUtility.sendSys3Message(pl, "[PK] Bandit", killer.getName() + " stop the <Outlaw> (" + looser.getName() + ") !");
			}
		});
	}
	
	public void onKill(Player player, Player diedPlayer)
	{
		player.setbanditKillStreak(player.getBanditKillStreak() + 1);
		if (player.getBanditKillStreak() == 5)
		{
			startBandit(player);
		}
		if (diedPlayer.getBanditKillStreak() > 0)
		{
			diedPlayer.setbanditKillStreak(0);
		}
	}
	
	private static class SingletonHolder
	{
		protected static final BanditService instance = new BanditService();
	}
	
	public static BanditService getInstance()
	{
		return SingletonHolder.instance;
	}
}