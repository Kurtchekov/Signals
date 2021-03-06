package com.minemaarten.signals.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import com.minemaarten.signals.tileentity.IGUITextFieldSensitive;

public class PacketUpdateTextfield extends LocationIntPacket<PacketUpdateTextfield>{

    private int textFieldID;
    private String text;

    public PacketUpdateTextfield(){}

    public PacketUpdateTextfield(TileEntity te, int textfieldID){
        super(te.getPos());
        textFieldID = textfieldID;
        text = ((IGUITextFieldSensitive)te).getText(textfieldID);
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(textFieldID);
        ByteBufUtils.writeUTF8String(buffer, text);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        textFieldID = buffer.readInt();
        text = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void handleClientSide(EntityPlayer player){}

    @Override
    public void handleServerSide(EntityPlayer player){
        TileEntity te = getTileEntity(player.getEntityWorld());
        if(te instanceof IGUITextFieldSensitive) {
            ((IGUITextFieldSensitive)te).setText(textFieldID, text);
        }
    }

}
