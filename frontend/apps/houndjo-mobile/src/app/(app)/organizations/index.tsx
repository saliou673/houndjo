import { useState } from 'react';
import { ScrollView, StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import {
  useGetMyOrganizations,
  useRegisterSchool,
  useUpdateOrganization,
  useGetMemberships,
  useList,
  useInvite,
  type Organization,
  type MembershipRoleEnumKey,
} from '@api-client';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Picker } from '@/components/ui/picker';
import { Text } from '@/components/ui/text';
import { showToast } from '@/components/toast/toast-store';

const PAGEABLE = { pageable: { page: 0, size: 100 } };
const INVITABLE_ROLES: MembershipRoleEnumKey[] = ['SCHOOL_ADMIN', 'TEACHER'];

type OrganizationFormProps = {
  organization?: Organization;
  saving: boolean;
  onSubmit: (values: { name: string; contactEmail: string }) => void;
};

function OrganizationForm({ organization, saving, onSubmit }: OrganizationFormProps) {
  const { t } = useTranslation();
  const [name, setName] = useState(organization?.name ?? '');
  const [contactEmail, setContactEmail] = useState(organization?.contactEmail ?? '');

  return (
    <>
      <Input label={t('organizations.name')} value={name} onChangeText={setName} />
      <Input
        label={t('organizations.email')}
        keyboardType="email-address"
        value={contactEmail}
        onChangeText={setContactEmail}
      />
      <Button
        label={saving ? t('organizations.saving') : t('organizations.save')}
        loading={saving}
        onPress={() => onSubmit({ name, contactEmail })}
      />
    </>
  );
}

export default function OrganizationsScreen() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [selectedId, setSelectedId] = useState<number | undefined>(undefined);
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState<MembershipRoleEnumKey>('TEACHER');

  const { data: organizations = [] } = useGetMyOrganizations();
  const selected = organizations.find((organization) => organization.id === selectedId) ?? organizations[0];
  const orgId = selected?.id;

  const { data: membersResult } = useGetMemberships(orgId ?? 0, PAGEABLE, undefined, {
    query: { enabled: !!orgId },
  });
  const { data: invitationsResult } = useList(orgId ?? 0, PAGEABLE, undefined, {
    query: { enabled: !!orgId },
  });
  const members = membersResult?.items ?? [];
  const invitations = invitationsResult?.items ?? [];

  const { mutate: registerSchool, isPending: isCreating } = useRegisterSchool({
    mutation: {
      onSuccess: async (data) => {
        await queryClient.invalidateQueries({ queryKey: [{ url: '/api/organizations/mine' }] });
        setSelectedId(data.id);
        showToast(t('organizations.savedToast'), 'success');
      },
    },
  });

  const { mutate: updateOrganization, isPending: isUpdating } = useUpdateOrganization({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({ queryKey: [{ url: '/api/organizations/mine' }] });
        showToast(t('organizations.savedToast'), 'success');
      },
    },
  });

  const { mutate: invite, isPending: isInviting } = useInvite({
    mutation: {
      onSuccess: async () => {
        if (orgId !== undefined) {
          await queryClient.invalidateQueries({
            queryKey: [{ url: '/api/organizations/:orgId/invitations', params: { orgId } }],
          });
        }
        setInviteEmail('');
        showToast(t('organizations.inviteSentToast'), 'success');
      },
    },
  });

  const saving = isCreating || isUpdating;

  function save(values: { name: string; contactEmail: string }) {
    if (selected?.id !== undefined) {
      updateOrganization({
        id: selected.id,
        data: {
          ...values,
          phoneNumber: selected.phoneNumber,
          address: selected.address,
          defaultCurrencyCode: selected.defaultCurrencyCode,
          defaultLanguageKey: selected.defaultLanguageKey,
          timezone: selected.timezone,
        },
      });
    } else {
      registerSchool({ data: values });
    }
  }

  function submitInvite() {
    if (orgId === undefined || !inviteEmail) return;
    invite({ orgId, data: { email: inviteEmail, role: inviteRole } });
  }

  return (
    <ScrollView contentContainerStyle={styles.content}>
      <Text variant="title">{t('organizations.title')}</Text>
      <Text variant="caption">{t('organizations.description')}</Text>
      <Card>
        <Text variant="subtitle">{t('organizations.schools')}</Text>
        {organizations.map((organization) => (
          <Button
            key={organization.id}
            variant={selected?.id === organization.id ? 'default' : 'outline'}
            label={organization.name}
            onPress={() => setSelectedId(organization.id)}
          />
        ))}
        {organizations.length === 0 && <Text variant="caption">{t('organizations.empty')}</Text>}
      </Card>
      <Card>
        <Text variant="subtitle">{selected ? t('organizations.edit') : t('organizations.create')}</Text>
        <OrganizationForm key={selected?.id ?? 'new'} organization={selected} saving={saving} onSubmit={save} />
      </Card>
      {selected && (
        <Card>
          <Text variant="subtitle">{t('organizations.members')}</Text>
          <Input
            label={t('organizations.inviteEmail')}
            keyboardType="email-address"
            value={inviteEmail}
            onChangeText={setInviteEmail}
          />
          <Picker
            variant="outline"
            value={inviteRole}
            onValueChange={(value) => setInviteRole(value as MembershipRoleEnumKey)}
            options={INVITABLE_ROLES.map((value) => ({ value, label: value }))}
          />
          <Button label={t('organizations.invite')} loading={isInviting} onPress={submitInvite} />
          {members.map((member) => (
            <Text key={member.id}>
              {member.userFullName} · {member.role}
            </Text>
          ))}
          {invitations.length > 0 && (
            <>
              <Text variant="subtitle">{t('organizations.pendingInvitations')}</Text>
              {invitations.map((invitation) => (
                <Text key={invitation.id} variant="caption">
                  {invitation.email} · {invitation.role}
                </Text>
              ))}
            </>
          )}
        </Card>
      )}
    </ScrollView>
  );
}
const styles = StyleSheet.create({ content: { padding: 16, gap: 16 } });
