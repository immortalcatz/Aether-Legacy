package com.legacy.aether.common.entities.projectile.darts;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.legacy.aether.common.entities.movement.AetherPoisonMovement;
import com.legacy.aether.common.items.ItemsAether;
import com.legacy.aether.common.networking.AetherNetworkingManager;
import com.legacy.aether.common.networking.packets.PacketSendPoison;
import com.legacy.aether.common.player.PlayerAether;

public class EntityDartPoison extends EntityDartBase
{

	public EntityLivingBase victim;

	public AetherPoisonMovement poison;

    public EntityDartPoison(World worldIn)
    {
        super(worldIn);
    }

    public EntityDartPoison(World world, EntityLivingBase entity) 
    {
		super(world, entity);
	}

    public void entityInit()
    {
        super.entityInit();
        this.setDamage(0);
    }

    public void onUpdate()
    {
        super.onUpdate();

        if (this.victim != null)
        {
        	if (this.victim.isDead || this.poison.poisonTime == 0)
        	{
        		this.setDead();

        		return;
        	}

        	if (this.shootingEntity != null)
        	{
                if (this.shootingEntity.worldObj instanceof WorldServer)
                {
                	((WorldServer)this.shootingEntity.worldObj).spawnParticle(EnumParticleTypes.ITEM_CRACK, this.victim.posX, this.victim.getEntityBoundingBox().minY + this.victim.height * 0.8D, this.victim.posZ, 2, 0.0D, 0.0D, 0.0D, 0.0625D, new int[] {Item.getIdFromItem(Items.DYE), 1});
                }
        	}

        	this.isDead = false;
        	this.poison.onUpdate();
        	this.setInvisible(true);
        	this.setPosition(this.victim.posX, this.victim.posY, this.victim.posZ);
        }
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer entityIn)
    {
    	if (this.victim == null)
    	{
    		super.onCollideWithPlayer(entityIn);
    	}
    }

    @Override
    protected void arrowHit(EntityLivingBase living)
    {
    	super.arrowHit(living);

    	this.victim = living;
    	this.poison = new AetherPoisonMovement(this.victim);
    	this.poison.afflictPoison();

    	if (living instanceof EntityPlayer)
    	{
            EntityPlayer ent = (EntityPlayer)living;

            if (!this.worldObj.isRemote)
            {
            	AetherNetworkingManager.sendToAll(new PacketSendPoison(ent));
            }
    	}

    	this.isDead = false;
    }

    @Override
    protected Entity findEntityOnPath(Vec3d start, Vec3d end)
    {
    	if (this.victim != null)
    	{
    		return null;
    	}

    	return super.findEntityOnPath(start, end);
    }

	@Override
	protected ItemStack getArrowStack() 
	{
		return new ItemStack(ItemsAether.dart, 1, 1);
	}

}