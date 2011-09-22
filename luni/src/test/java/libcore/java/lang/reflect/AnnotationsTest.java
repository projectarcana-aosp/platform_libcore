/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;

public final class AnnotationsTest extends TestCase {

    public void testClassDirectAnnotations() {
        assertAnnotatedElement(Type.class, AnnotationA.class, AnnotationB.class);
    }

    public void testClassInheritedAnnotations() {
        assertAnnotatedElement(ExtendsType.class, AnnotationB.class);
    }

    public void testConstructorAnnotations() throws Exception {
        Constructor<Type> constructor = Type.class.getConstructor();
        assertAnnotatedElement(constructor, AnnotationA.class, AnnotationC.class);
    }

    public void testFieldAnnotations() throws Exception {
        Field field = Type.class.getField("field");
        assertAnnotatedElement(field, AnnotationA.class, AnnotationD.class);
    }

    public void testMethodAnnotations() throws Exception {
        Method method = Type.class.getMethod("method", String.class, String.class);
        assertAnnotatedElement(method, AnnotationB.class, AnnotationC.class);
    }

    public void testParameterAnnotations() throws Exception {
        Method method = Type.class.getMethod("method", String.class, String.class);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        assertEquals(2, parameterAnnotations.length);
        assertEquals(set(AnnotationB.class, AnnotationD.class),
                annotationsToTypes(parameterAnnotations[0]));
        assertEquals(set(AnnotationC.class, AnnotationD.class),
                annotationsToTypes(parameterAnnotations[1]));
    }

    public void testAnnotationDefaults() throws Exception {
        assertEquals((byte) 5, defaultValue("a"));
        assertEquals((short) 6, defaultValue("b"));
        assertEquals(7, defaultValue("c"));
        assertEquals(8L, defaultValue("d"));
        assertEquals(9.0f, defaultValue("e"));
        assertEquals(10.0, defaultValue("f"));
        assertEquals('k', defaultValue("g"));
        assertEquals(true, defaultValue("h"));
        assertEquals(Breakfast.WAFFLES, defaultValue("i"));
        assertEquals("@" + AnnotationA.class.getName() + "()", defaultValue("j").toString());
        assertEquals("maple", defaultValue("k"));
        assertEquals(AnnotationB.class, defaultValue("l"));
        assertEquals("[1, 2, 3]", Arrays.toString((int[]) defaultValue("m")));
        assertEquals("[WAFFLES, PANCAKES]", Arrays.toString((Breakfast[]) defaultValue("n")));
        assertEquals(null, defaultValue("o"));
        assertEquals(null, defaultValue("p"));
    }

    private Object defaultValue(String name) throws NoSuchMethodException {
        return HasDefaultsAnnotation.class.getMethod(name).getDefaultValue();
    }

    public void testEnclosingClass() {
        assertEquals(AnnotationsTest.class, Foo.class.getEnclosingClass());
        assertNull(AnnotationsTest.class.getEnclosingConstructor());
        assertNull(AnnotationsTest.class.getEnclosingMethod());
    }

    public void testEnclosingConstructor() throws Exception {
        Foo foo = new Foo("string");
        assertNull(foo.c.getEnclosingClass());
        assertEquals(Foo.class.getDeclaredConstructor(String.class),
                foo.c.getEnclosingConstructor());
        assertNull(foo.c.getEnclosingMethod());
    }

    public void testEnclosingMethod() throws Exception {
        Foo foo = new Foo();
        foo.foo("string");
        assertNull(foo.c.getEnclosingClass());
        assertNull(foo.c.getEnclosingConstructor());
        assertEquals(Foo.class.getDeclaredMethod("foo", String.class),
                foo.c.getEnclosingMethod());
    }

    public void testGetClasses() throws Exception {
        // getClasses() doesn't include classes inherited from interfaces!
        assertSetEquals(HasMemberClasses.class.getClasses(),
                HasMemberClassesSuperclass.B.class, HasMemberClasses.H.class);
    }

    public void testGetDeclaredClasses() throws Exception {
        assertSetEquals(HasMemberClasses.class.getDeclaredClasses(),
                HasMemberClasses.G.class, HasMemberClasses.H.class, HasMemberClasses.I.class,
                HasMemberClasses.J.class, HasMemberClasses.K.class, HasMemberClasses.L.class);
    }

    private static class Foo {
        Class<?> c;
        private Foo() {
        }
        private Foo(String s) {
            c = new Object() {}.getClass();
        }
        private Foo(int i) {
            c = new Object() {}.getClass();
        }
        private void foo(String s) {
            c = new Object() {}.getClass();
        }
        private void foo(int i) {
            c = new Object() {}.getClass();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationA {}

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationB {}

    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationC {}

    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationD {}

    @AnnotationA @AnnotationB
    public static class Type {
        @AnnotationA @AnnotationC public Type() {}
        @AnnotationA @AnnotationD public String field;
        @AnnotationB @AnnotationC public void method(@AnnotationB @AnnotationD String parameter1,
                @AnnotationC @AnnotationD String parameter2) {}
    }

    public static class ExtendsType extends Type {}

    static enum Breakfast { WAFFLES, PANCAKES }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface HasDefaultsAnnotation {
        byte a() default 5;
        short b() default 6;
        int c() default 7;
        long d() default 8;
        float e() default 9.0f;
        double f() default 10.0;
        char g() default 'k';
        boolean h() default true;
        Breakfast i() default Breakfast.WAFFLES;
        AnnotationA j() default @AnnotationA();
        String k() default "maple";
        Class l() default AnnotationB.class;
        int[] m() default { 1, 2, 3 };
        Breakfast[] n() default { Breakfast.WAFFLES, Breakfast.PANCAKES };
        Breakfast o();
        int p();
    }

    static class HasMemberClassesSuperclass {
        class A {}
        public class B {}
        static class C {}
    }

    public interface HasMemberClassesInterface {
        class D {}
        public class E {}
        static class F {}
    }

    public static class HasMemberClasses extends HasMemberClassesSuperclass
            implements HasMemberClassesInterface {
        class G {}
        public class H {}
        static class I {}
        enum J {}
        interface K {}
        @interface L {}
    }

    private void assertAnnotatedElement(
            AnnotatedElement element, Class<? extends Annotation>... expectedAnnotations) {
        Set<Class<? extends Annotation>> actualTypes = annotationsToTypes(element.getAnnotations());
        Set<Class<? extends Annotation>> expectedTypes = set(expectedAnnotations);
        assertEquals(expectedTypes, actualTypes);

        // getAnnotations() should be consistent with isAnnotationPresent() and getAnnotation()
        assertPresent(expectedTypes.contains(AnnotationA.class), element, AnnotationA.class);
        assertPresent(expectedTypes.contains(AnnotationB.class), element, AnnotationB.class);
        assertPresent(expectedTypes.contains(AnnotationC.class), element, AnnotationC.class);

        try {
            element.isAnnotationPresent(null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            element.getAnnotation(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    private Set<Class<? extends Annotation>> annotationsToTypes(Annotation[] annotations) {
        Set<Class<? extends Annotation>> result = new HashSet<Class<? extends Annotation>>();
        for (Annotation annotation : annotations) {
            result.add(annotation.annotationType());
        }
        return result;
    }

    private void assertPresent(boolean present, AnnotatedElement element,
            Class<? extends Annotation> annotation) {
        if (present) {
            assertNotNull(element.getAnnotation(annotation));
            assertTrue(element.isAnnotationPresent(annotation));
        } else {
            assertNull(element.getAnnotation(annotation));
            assertFalse(element.isAnnotationPresent(annotation));
        }
    }

    private <T> Set<T> set(T... instances) {
        return new HashSet<T>(Arrays.asList(instances));
    }

    private void assertSetEquals(Object[] actual, Object... expected) {
        Set<Object> actualSet = new HashSet<Object>(Arrays.asList(actual));
        Set<Object> expectedSet = new HashSet<Object>(Arrays.asList(expected));
        assertEquals(expectedSet, actualSet);
    }
}
