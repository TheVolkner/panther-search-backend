package ve.com.tps.panthersearchengine.entities;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name="webpage")
public class WebPage{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idwebpage")
    private Integer idWebPage;
    private String url;
    private String title;
    private String description;

    //DEFINIMOS UN CONSTRUCTOR CON UN ÚNICO PARÁMETRO QUE SERÁ EL URL
    public WebPage(String url){

        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        WebPage webPage = (WebPage) o;
        return idWebPage != null && Objects.equals(idWebPage, webPage.idWebPage);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}