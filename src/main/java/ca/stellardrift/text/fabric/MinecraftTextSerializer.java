/*
 * Copyright © 2020 zml [at] stellardrift [.] ca
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the “Software”), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ca.stellardrift.text.fabric;

import ca.stellardrift.text.fabric.mixin.AccessorTextSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;


class MinecraftTextSerializer implements ComponentSerializer<Component, Component, Text> {

  MinecraftTextSerializer() {
  }

  @Override
  public Component deserialize(Text input) {
    if(input instanceof ComponentText) {
      return ((ComponentText) input).getWrapped();
    }

    return AccessorTextSerializer.getGSON().fromJson(Text.Serializer.toJsonTree(input), Component.class);
  }

  @Override
  public MutableText serialize(Component component) {
    return Text.Serializer.fromJson(AccessorTextSerializer.getGSON().toJsonTree(component));
  }
}
