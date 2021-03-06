package com.legacy.aether.common.entities.bosses.slider;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.legacy.aether.common.Aether;
import com.legacy.aether.common.blocks.BlocksAether;
import com.legacy.aether.common.blocks.dungeon.BlockDungeonBase;
import com.legacy.aether.common.blocks.util.EnumStoneType;
import com.legacy.aether.common.entities.util.AetherNameGen;
import com.legacy.aether.common.items.ItemsAether;
import com.legacy.aether.common.items.tools.ItemAetherTool;
import com.legacy.aether.common.items.util.EnumAetherToolType;
import com.legacy.aether.common.player.PlayerAether;
import com.legacy.aether.common.registry.achievements.AchievementsAether;
import com.legacy.aether.common.registry.sounds.SoundsAether;

public class EntitySlider extends EntityFlying 
{

	public static final DataParameter<String> SLIDER_NAME = EntityDataManager.<String>createKey(EntitySlider.class, DataSerializers.STRING);

	public static final DataParameter<Boolean> SLIDER_AWAKE = EntityDataManager.<Boolean>createKey(EntitySlider.class, DataSerializers.BOOLEAN);

	private int dungeonX, dungeonY, dungeonZ;

	public float hurtAngle, hurtAngleX, hurtAngleZ;

	public int chatTime, moveTime;

	public boolean isMoving, crushedBlock;

	public float velocity;

	public EnumFacing direction;

	public EntitySlider(World world) 
	{
		super(world);
		this.setSize(2.0F, 2.0F);
		this.rotationYaw = this.rotationPitch = 0.0F;
	}

	@Override
    protected void applyEntityAttributes()
    {
    	super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(500.0D);
        this.setHealth(500.0F);
    }

	@Override
	protected void entityInit()
	{
		super.entityInit();

		this.posX = Math.floor(this.posX + 0.5D);
		this.posY = Math.floor(this.posY + 0.5D);
		this.posZ = Math.floor(this.posZ + 0.5D);

	    this.dataManager.register(SLIDER_AWAKE, false);
	    this.dataManager.register(SLIDER_NAME, String.valueOf(AetherNameGen.gen()));
	}

    @Override
	protected SoundEvent getAmbientSound()
    {
        return SoundEvents.AMBIENT_CAVE;
    }

    @Override
    protected SoundEvent getHurtSound()
    {
        return SoundEvents.BLOCK_STONE_STEP;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return null;
    }

    @Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) 
	{
        super.writeEntityToNBT(nbttagcompound);

		nbttagcompound.setInteger("dungeonX", this.dungeonX);
		nbttagcompound.setInteger("dungeonY", this.dungeonY);
		nbttagcompound.setInteger("dungeonZ", this.dungeonZ);

        nbttagcompound.setBoolean("isAwake", this.isAwake());
		nbttagcompound.setString("bossName", this.getBossName());
    }

    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);

        this.dungeonX = nbttagcompound.getInteger("dungeonX");
        this.dungeonY = nbttagcompound.getInteger("dungeonY");
        this.dungeonZ = nbttagcompound.getInteger("dungeonZ");

        this.setAwake(nbttagcompound.getBoolean("isAwake"));
        this.setBossName(nbttagcompound.getString("bossName"));
    }

	public boolean criticalCondition() 
	{
		return this.getHealth() <= 80;
	}

    @Override
	public void onUpdate()
	{
		if(this.hurtAngle > 0.01F) 
		{
			this.hurtAngle *= 0.8F;
		}

		if(this.chatTime > 0) 
		{
			this.chatTime --;
		}

		super.onUpdate();

		this.renderYawOffset = this.rotationPitch = this.rotationYaw = 0.0F;

		if (!this.isAwake())
		{
			this.setAttackTarget(null);
			return;
		}

		if(!this.worldObj.isRemote) 
		{
			if (this.getAttackTarget() == null || this.getAttackTarget().isDead || this.getAttackTarget().getHealth() <= 0.0F) 
			{
				this.reset();

				return;
			}

			if(this.isMoving) 
			{
				if(this.isCollided) 
				{
					double x, y, z;
					x = posX - 0.5D;
					y = this.getEntityBoundingBox().minY + 0.75D;
					z = posZ - 0.5D;

					this.crushedBlock = false;

					if(y < 124D && y > 4D) 
					{
						if(this.direction == EnumFacing.UP)
						{
							for(int i = 0; i < 25; i++)
							{
								double a = (double)((i / 5) - 2) * 0.75D;
								double b = (double)((i % 5) - 2) * 0.75D;
								destroyBlock(new BlockPos.MutableBlockPos().setPos(x + a, y + 1.5D, z + b));
							}
						}
						else if(this.direction == EnumFacing.DOWN)
						{
							for(int i = 0; i < 25; i++) 
							{
								double a = (double)((i / 5) - 2) * 0.75D;
								double b = (double)((i % 5) - 2) * 0.75D;
								destroyBlock(new BlockPos.MutableBlockPos().setPos(x + a, y - 1.5D, z + b));
							}
						} 
						else if(this.direction == EnumFacing.EAST)
						{
							for(int i = 0; i < 25; i++) {
								double a = (double)((i / 5) - 2) * 0.75D;
								double b = (double)((i % 5) - 2) * 0.75D;
								destroyBlock(new BlockPos.MutableBlockPos().setPos(x + 1.5D, y + a, z + b));
							}
						}
						else if(this.direction == EnumFacing.WEST)
						{
							for(int i = 0; i < 25; i++) {
								double a = (double)((i / 5) - 2) * 0.75D;
								double b = (double)((i % 5) - 2) * 0.75D;
								destroyBlock(new BlockPos.MutableBlockPos().setPos(x - 1.5D, y + a, z + b));
							}
						} 
						else if(this.direction == EnumFacing.SOUTH) 
						{
							for(int i = 0; i < 25; i++) 
							{
								double a = (double)((i / 5) - 2) * 0.75D;
								double b = (double)((i % 5) - 2) * 0.75D;
								destroyBlock(new BlockPos.MutableBlockPos().setPos(x + a, y + b, z + 1.5D));
							}
						}
						else if(this.direction == EnumFacing.NORTH)
						{
							for(int i = 0; i < 25; i++) 
							{
								double a = (double)((i / 5) - 2) * 0.75D;
								double b = (double)((i % 5) - 2) * 0.75D;
								destroyBlock(new BlockPos.MutableBlockPos().setPos(x + a, y + b, z - 1.5D));
							}
						}
					}

					if(this.crushedBlock)
					{
						this.worldObj.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 3F, (0.625F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
						this.worldObj.playSound(null, posX, posY, posZ, SoundsAether.slider_collide, SoundCategory.HOSTILE, 2.5F, 1.0F / (this.rand.nextFloat() * 0.2F + 0.9F));
					}

					this.stop();
				} 
				else 
				{
					if(this.velocity < 2.0F)
					{
						this.velocity += (this.criticalCondition() ? 0.07F : 0.035F);
					}

					this.motionX = this.motionY = this.motionZ = 0.0F;

					if(this.direction == EnumFacing.UP)
					{
						this.motionY = this.velocity;

						if(this.getEntityBoundingBox().minY > this.getAttackTarget().getEntityBoundingBox().minY + 0.35D) 
						{
							this.stop();
							this.moveTime = this.criticalCondition() ? 4 : 8;
						}
					}
					else if(this.direction == EnumFacing.DOWN)
					{
						this.motionY = -this.velocity;

						if(this.getEntityBoundingBox().minY < this.getAttackTarget().getEntityBoundingBox().minY - 0.25D) 
						{
							this.stop();
							this.moveTime = this.criticalCondition() ? 4 : 8;
						}
					} 
					else if(this.direction == EnumFacing.EAST)
					{
						this.motionX = this.velocity;

						if(this.posX > this.getAttackTarget().posX + 0.125D) 
						{
							this.stop();
							this.moveTime = this.criticalCondition() ? 4 : 8;
						}
					}
					else if(this.direction == EnumFacing.WEST) 
					{
						this.motionX = -this.velocity;

						if(this.posX < this.getAttackTarget().posX - 0.125D) 
						{
							this.stop();
							this.moveTime = this.criticalCondition() ? 4 : 8;
						}
					}
					else if(this.direction == EnumFacing.SOUTH) 
					{
						this.motionZ = this.velocity;

						if(this.posZ > this.getAttackTarget().posZ + 0.125D) 
						{
							this.stop();
							this.moveTime = this.criticalCondition() ? 4 : 8;
						}
					} 
					else if(this.direction == EnumFacing.NORTH)
					{
						this.motionZ = -this.velocity;

						if(this.posZ < this.getAttackTarget().posZ - 0.125D)
						{
							this.stop();
							this.moveTime = this.criticalCondition() ? 4 : 8;
						}
					}
				}
			}
			else
			{
				if(this.moveTime > 0) 
				{
					this.moveTime--;

					if(this.criticalCondition() && this.rand.nextInt(2) == 0)
					{
						this.moveTime--;
					}

					this.motionX = this.motionY = this.motionZ = 0.0F;
				}
				else 
				{
					if (this.getAttackTarget() == null) return;

					double a, b, c;
					a = Math.abs(this.posX - this.getAttackTarget().posX);
					b = Math.abs(this.getEntityBoundingBox().minY - this.getAttackTarget().getEntityBoundingBox().minY);
					c = Math.abs(this.posZ - this.getAttackTarget().posZ);

					if(a > c) 
					{
						this.direction = EnumFacing.EAST;

						if(this.posX > this.getAttackTarget().posX)
						{
							this.direction = EnumFacing.WEST;
						}
					} 
					else 
					{
						this.direction = EnumFacing.SOUTH;

						if(this.posZ > this.getAttackTarget().posZ) 
						{
							this.direction = EnumFacing.NORTH;
						}
					}

					if((b > a && b > c) || (b > 0.25F && this.rand.nextInt(5) == 0))
					{
						this.direction = EnumFacing.UP;

						if(this.posY > this.getAttackTarget().posY)
						{
							this.direction = EnumFacing.DOWN;
						}
					}

					this.worldObj.playSound(null, posX, posY, posZ, SoundsAether.slider_move, SoundCategory.HOSTILE, 2.5F, 1.0F / (this.rand.nextFloat() * 0.2F + 0.9F));
					this.isMoving = true;
				}
			}
		}
	}

	private void destroyBlock(BlockPos pos)
	{
		IBlockState state = this.worldObj.getBlockState(pos);

		if(state.getBlock() == Blocks.AIR || state.getBlock() instanceof BlockDungeonBase) 
		{
			return;
		}

		Aether.proxy.spawnBlockBrokenFX(state, pos);

		state.getBlock().breakBlock(this.worldObj, pos, state);
		state.getBlock().dropBlockAsItem(this.worldObj, pos, state, 0);

		this.worldObj.setBlockToAir(pos);

		this.crushedBlock = true;

		Aether.proxy.spawnSmoke(this.worldObj, pos);
	}

	private void openDoor()
	{
		int x = this.dungeonX + 15;

		for(int y = this.dungeonY + 1; y < this.dungeonY + 5; y++)
		{
			for(int z = this.dungeonZ + 6; z < this.dungeonZ + 10; z++)
			{
				this.worldObj.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState());
			}
		}
	}

	@Override
	public void applyEntityCollision(Entity entity) 
	{
		if (this.isAwake() && this.isMoving)
		{
			boolean flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), 6);

			if(flag && entity instanceof EntityLivingBase)
			{
				EntityLivingBase collidedEntity = (EntityLivingBase)entity;
				collidedEntity.addVelocity(collidedEntity.motionY, 0.35D, collidedEntity.motionZ);
				this.worldObj.playSound(null, posX, posY, posZ, SoundsAether.slider_collide, SoundCategory.HOSTILE, 2.5F, 1.0F / (this.rand.nextFloat() * 0.2F + 0.9F));

				this.stop();
			}
		}
    }

	@Override
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) 
	{
		this.dropItem(Item.getItemFromBlock(BlocksAether.dungeon_block), 7 + rand.nextInt(3));

		this.entityDropItem(new ItemStack(ItemsAether.dungeon_key), 0.5F);
    }

    @Override
    public EntityItem entityDropItem(ItemStack stack, float offsetY)
    {
        if (stack.stackSize != 0 && stack.getItem() != null)
        {
            EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY + (double)offsetY, this.posZ, stack);
            entityitem.setEntityInvulnerable(true);
            entityitem.setDefaultPickupDelay();
            if (captureDrops)
                this.capturedDrops.add(entityitem);
            else
                this.worldObj.spawnEntityInWorld(entityitem);
            return entityitem;
        }
        else
        {
            return null;
        }
    }

	public void stop() 
	{
		this.isMoving = false;
		this.moveTime = 12;
		this.direction = EnumFacing.UP;
		this.motionX = this.motionY = this.motionZ = this.velocity = 0.0F;
	}

    private void sendMessage(EntityPlayer player, String s)
    {
		Side side = FMLCommonHandler.instance().getEffectiveSide();

        if (this.chatTime <= 0)
        {
        	if (side.isClient())
        	{
                Aether.proxy.sendMessage(player, s);
        	}

            this.chatTime = 60;
        }
    }

	@Override
	public boolean attackEntityFrom(DamageSource ds, float var2)
	{
		if(ds.getEntity() == null || !(ds.getEntity() instanceof EntityPlayer))
		{
			return false;
		}

		EntityPlayer player = (EntityPlayer)ds.getEntity();
		ItemStack stack = player.inventory.getCurrentItem();

		if (stack == null || stack.getItem() == null)
		{
			return false;
		}

		if (!(stack.getItem() instanceof ItemPickaxe) && !(stack.getItem() instanceof ItemAetherTool))
		{
			this.sendMessage(player, "Hmm. Perhaps I need to attack it with a Pickaxe?"); 

			return false; 
		}

		if (stack.getItem() instanceof ItemAetherTool && ((ItemAetherTool)stack.getItem()).toolType != EnumAetherToolType.PICKAXE)
		{
			this.sendMessage(player, "Hmm. Perhaps I need to attack it with a Pickaxe?"); 

			return false; 
		}

		boolean flag = super.attackEntityFrom(ds, Math.max(0, var2));
	
		if(flag)
		{
			for(int j = 0; j < (this.getHealth() <= 0 ? 2 : 4); j++) 
			{
				double a = posX + ((rand.nextFloat() - rand.nextFloat()) * 1.5D);
				double b = this.getEntityBoundingBox().minY + 1.75D;
				double c = posZ + ((rand.nextFloat() - rand.nextFloat()) * 1.5D);

				if(this.getHealth() <= 0)
				{
					this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, a, b, c, 0.0D, 0.0D, 0.0D);
				} 

				Aether.proxy.spawnBlockBrokenFX(Blocks.STONE.getDefaultState(), new BlockPos(a, b, c));
			}

			if(this.getHealth() <= 0 || this.isDead)
			{
				openDoor();
				unlockBlock(new BlockPos(dungeonX, dungeonY, dungeonZ));

				IBlockState state = Blocks.TRAPDOOR.getDefaultState();
				this.worldObj.setBlockState(new BlockPos(dungeonX + 7, dungeonY + 1, dungeonZ + 7), state.withProperty(BlockTrapDoor.FACING, EnumFacing.SOUTH), 2);
				this.worldObj.setBlockState(new BlockPos(dungeonX + 8, dungeonY + 1, dungeonZ + 7), state.withProperty(BlockTrapDoor.FACING, EnumFacing.SOUTH), 2);
				this.worldObj.setBlockState(new BlockPos(dungeonX + 7, dungeonY + 1, dungeonZ + 8), state.withProperty(BlockTrapDoor.FACING, EnumFacing.NORTH), 2);
				this.worldObj.setBlockState(new BlockPos(dungeonX + 8, dungeonY + 1, dungeonZ + 8), state.withProperty(BlockTrapDoor.FACING, EnumFacing.NORTH), 2);
				PlayerAether.get(player).setCurrentBoss(null);
				player.addStat(AchievementsAether.defeat_bronze);
				this.worldObj.playSound(null, posX, posY, posZ, SoundsAether.slider_death, SoundCategory.HOSTILE, 2.5F, 1.0F / (this.rand.nextFloat() * 0.2F + 0.9F));
				this.isDead = true;
			}

			if(!this.isAwake())
			{
				this.worldObj.playSound(null, posX, posY, posZ, SoundsAether.slider_awaken, SoundCategory.HOSTILE, 2.5F, 1.0F / (this.rand.nextFloat() * 0.2F + 0.9F));
				this.setAttackTarget(player);

				int x = this.dungeonX + 15;

				for(int y = this.dungeonY + 1; y < this.dungeonY + 8; y++)
				{
					for(int z = this.dungeonZ + 5; z < this.dungeonZ + 11; z++)
					{
						this.worldObj.setBlockState(new BlockPos(x, y, z), BlocksAether.locked_dungeon_block.getDefaultState());
					}
				}

				this.setAwake(true);
			} 

			if(this.isMoving) 
			{
				this.velocity *= 0.75F;
			}
		}

		double a, c;

		a = Math.abs(this.posX - player.posX);
		c = Math.abs(this.posZ - player.posZ);

		if(a > c)
		{
			this.hurtAngleZ = 1;
			this.hurtAngleX = 0;

			if(this.posX > player.posX)
			{
				this.hurtAngleZ = -1;
			}
		}
		else
		{
			this.hurtAngleX = 1;
			this.hurtAngleZ = 0;

			if(this.posZ > player.posZ) 
			{
				this.hurtAngleX = -1;
			}
		}

		this.hurtAngle = 0.7F - (this.getHealth() / 875F);

		PlayerAether.get(player).setCurrentBoss(this);

		return flag;
	}

	private void unlockBlock(BlockPos pos)
	{	
		IBlockState blockState = this.worldObj.getBlockState(pos);
		Block block = blockState.getBlock();

		if(block == BlocksAether.locked_dungeon_block)
		{
			this.worldObj.setBlockState(pos, BlocksAether.dungeon_block.getDefaultState().withProperty(BlockDungeonBase.dungeon_stone, EnumStoneType.getType(block.getMetaFromState(blockState))), 2);
			this.unlockBlock(pos.east());
			this.unlockBlock(pos.west());
			this.unlockBlock(pos.up());
			this.unlockBlock(pos.down());
			this.unlockBlock(pos.south());
			this.unlockBlock(pos.north());
		}
	}	

	@Override
    protected void collideWithNearbyEntities()
    {
        List<?> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));

        if (list != null && !list.isEmpty())
        {
            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity = (Entity)list.get(i);

                this.applyEntityCollision(entity);
            }
        }
    }

	@Override
	public boolean canDespawn()
	{
		return false;
	}

	@Override
	public boolean canBePushed()
	{
		return false;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return !this.isDead;
	}

	@Override
	public void addVelocity(double d, double d1, double d2)
	{
	}

	@Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z)
    {
    }

	@Override
	public void knockBack(Entity par1Entity, float par2, double par3, double par5)
	{

	}

	public void reset()
	{
		this.moveTime = 0;

		this.stop();
		this.openDoor();
		this.setAwake(false);
		this.setAttackTarget(null);
		this.setHealth(this.getMaxHealth());
		this.setPositionAndUpdate(this.dungeonX + 8, this.dungeonY + 2, this.dungeonZ + 8);
	}

	public void setDungeon(double posX, double posY, double posZ)
	{
		this.dungeonX = (int) posX;
		this.dungeonY = (int) posY;
		this.dungeonZ = (int) posZ;
	}

	public void setBossName(String name)
	{
		this.dataManager.set(SLIDER_NAME, name);
	}

	public String getBossName()
	{
		return this.dataManager.get(SLIDER_NAME);
	}

	public String getBossTitle()
	{
		return this.getBossName() + ", the Slider";
	}

	public void setAwake(boolean isAwake)
	{
		this.dataManager.set(SLIDER_AWAKE, isAwake);
	}

	public boolean isAwake()
	{
		return this.dataManager.get(SLIDER_AWAKE);
	}

}