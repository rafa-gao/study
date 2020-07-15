package classloader;

/**
 * @author rafa gao
 */
public class ClassLoadTest {

    /**
     * 一个使用父类加载器去加载
     * 另外一个使用子类加载器去加载
     *
     * @param args
     */
    public static void main(String[] args) throws ClassNotFoundException {

        FatherCl fatherCl = new FatherCl();

        SonCl sonCl = new SonCl(fatherCl);

        System.out.println("FonCl father classLoader:" + sonCl.getParent());
        System.out.println("FatherCl father classLoader:" + fatherCl.getParent());

        Class<?> aClass1 = fatherCl.loadClass("classloader.Test");
        System.out.println("FatherCl :"+aClass1.getClassLoader());

    }

    private static class FatherCl extends ClassLoader{
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return null;
        }
    }

    public static class SonCl extends ClassLoader{

        public SonCl(ClassLoader parent) {
            super(parent);
        }

    }


}
