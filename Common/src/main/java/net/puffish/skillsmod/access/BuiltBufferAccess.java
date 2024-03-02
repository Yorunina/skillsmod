package net.puffish.skillsmod.access;

import net.minecraft.util.math.Matrix4f;

import java.util.List;

public interface BuiltBufferAccess {
	void setEmits(List<Matrix4f> emits);

	List<Matrix4f> getEmits();
}
