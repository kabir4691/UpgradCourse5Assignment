package com.upgrad.quora.service.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "answer", schema = "public")
@NamedQueries({
        @NamedQuery(name = "answerByAnswerId", query = "select q from AnswerEntity q where q.uuid = :answerId"),
        @NamedQuery(name = "allAnswersByQuestionId", query = "select a from AnswerEntity a where a.questionId.id = :questionId")
})
public class AnswerEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "UUID")
    @Size(max = 200)
    private String uuid;

    @Column(name = "ANS")
    @NotNull
    @Size(max = 255)
    private String ans;


    @Column(name = "DATE")
    private ZonedDateTime date;


    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private UserEntity userId;


    @ManyToOne
    @JoinColumn(name = "QUESTION_ID")
    private QuestionEntity questionId;

    public UserEntity getUserId() {
        return userId;
    }

    public void setUserId(UserEntity userId) {
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAns() {
        return ans;
    }

    public void setAns(String ans) {
        this.ans = ans;
    }

    public QuestionEntity getQuestionId() {
        return questionId;
    }

    public void setQuestionId(QuestionEntity questionId) {
        this.questionId = questionId;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object obj) {
        return new EqualsBuilder().append(this, obj).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this).hashCode();
    }

    @Override
    public String toString() {
        return "AnswerEntity[id=" + getId() + ",uuid=" + getUuid() + "]";
    }
}
