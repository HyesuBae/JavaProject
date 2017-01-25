package jpabook.start;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

/**
 * Created by hyesubae on 16. 7. 17.
 */
public class JpaMain {
    public static void main(String[] args) {
        // persistence.xml에서 "jpabook"이라는 이름의 persistence-unit을 찾아서 엔티티매니저 팩토리 생성
        // EMF를 생성하는 비용은 아주 크므로 애플리케이션 전체에서 딱 한번만 생성하고 공유해서 사용해야함.
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");

        // JPA의 대부분의 기능은 엔티티매니저가 제공. 엔티티매니저를 사용해서 엔티티를 디비에 CRUD.
        // 엔티티매니저는 내부에 데이터소스(데이터베이스 커넥션)을 유지하면서 디비와 통신한다.
        // 개발자는 엔티티매니저를 가상의 데이터베이스라고 생각할 수 있음.
        // 엔티티매니저는 데이터베이스 커넥션과 밀접한 관계가 있으므로 쓰레드간에 공유하거나 재사용 하면 안 됨.
        EntityManager em = emf.createEntityManager();

        // JPA를 사용할 땐 항상 트랜잭션 안에서 데이터를 변경해야한다.
        EntityTransaction tx = em.getTransaction();

        try{
            tx.begin();
            logic(em);
            tx.commit();
        }catch(Exception e){
            e.printStackTrace();
            tx.rollback();
        }finally {
            em.close();
        }

        emf.close();
    }

    // Business Logic
    private static void logic(EntityManager em){
        String id = "id1";
        Member member = new Member();
        member.setId(id);
        member.setUsername("Hyesu");
        member.setAge(25);

        em.persist(member);

        //JPA는 어떤 엔티티가 변경되었는지 추적하는 기능이 있어서 엔티티의 값이 변경되면
        //알아서 update문 실행함. 즉, em.update() 같은 메소드는 따로 없음.
        member.setAge(26);

        Member findMember = em.find(Member.class, id);
        System.out.println(findMember.getId()+" "+findMember.getAge()+" "+findMember.getUsername());

        // JPA는 엔티티 객체를 중심으로 개발하기 떄문에 검색할 떄도 테이블이 아닌 엔티티 객체를 대상으로 검색해야함.
        // 그래서 테이블이 아닌 엔티티 객체를 대상으로 검색하려면 디비의 모든 데이터를 앱으로 불러와서 엔티티 객체로 변경한 후 검색해야함.
        // 이는 사실상 불가능. 결국 필요한 데이터만 불러오려면 검색 조건이 포함된 SQL을 사용해야함.
        // JPA는 JPQL이라는 쿼리 언어로 이 문제를 해결.
        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
        System.out.println("members size:"+members.size());

        em.remove(member);
    }
}
